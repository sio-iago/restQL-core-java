(ns restql.core.async-request
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clojure.core.async :as a :refer [chan go go-loop >! <!]]
            [org.httpkit.client :as http]
            [restql.core.async-request-builder :as builder]
            [restql.core.query :as query]
            [restql.core.hooks.core :as hook]
            [restql.core.log :refer [debug error]]
            [restql.core.extractor :refer [traverse]]
            [slingshot.slingshot :refer [try+]]
            [cheshire.core :as json]
            [clojure.walk :refer [stringify-keys keywordize-keys]])
    (:import [java.net URLDecoder URI]
             (javax.net.ssl SSLEngine SSLParameters SNIHostName)))

(defonce MAX_RETRIES 1)

(defn- retry-on-error [request-func callback retries]
  (let [response @(request-func)]
    (if (or (zero? retries) (builder/success? [:_ response]))
      (-> @(request-func)
          callback)
      (retry-on-error request-func callback (dec retries)))))

(defn get-service-endpoint [mappings entity]
  (if (nil? (mappings entity))
    (throw+ (str "Resource endpoint not found" entity))
    (mappings entity)))

(defn fmap [f m]
  (reduce-kv #(assoc %1 %2 (f %3)) {} m))

(defn decode-url [string]
  (try
    (URLDecoder/decode string "utf-8")
    (catch Exception e
      string)))

(defn sni-configure
    [^SSLEngine ssl-engine ^URI uri]
    (let [^SSLParameters ssl-params (.getSSLParameters ssl-engine)]
        (.setServerNames ssl-params [(SNIHostName. (.getHost uri))])
        (.setSSLParameters ssl-engine ssl-params)))

(def client (http/make-client {:ssl-configurer sni-configure}))

(defn parse-query-params
  "this function takes a request object (with :url and :query-params)
  and transforms query params that are sets into vectors"
  [request]
  (update-in request [:query-params]
             #(fmap (fn [query-param-value]
                      (if (or (sequential? query-param-value) (set? query-param-value))
                        (->> query-param-value (map decode-url) (into []))
                        (decode-url query-param-value))) %)))

(defn convert-response [{:keys [status body headers]} {:keys [debugging metadata time url params timeout resource]}]
  (let [parsed (if (string? body) body (slurp body))
        base {:status        status
              :headers       headers
              :url           url
              :metadata      metadata
              :timeout       timeout
              :params        params
              :resource      resource
              :response-time time}]
    (try
      (assoc base
        :body (json/parse-string parsed true))
      (catch Exception e
        (error {:message (.getMessage e)}
               "error parsing request")
        (assoc base
          :parse-error true
          :body parsed)))))

(defn request-callback [result & {:keys [request
                                         request-timeout
                                         time-before
                                         query-opts
                                         output-ch
                                         ]}]
  (let [log-data {:resource (:resource request)
                  :timeout  request-timeout
                  :success  true}]
    (if (and
          (not (nil? result))
          (nil? (:error result)))
      (do
        (debug (assoc log-data :success true
                               :status (:status result)
                               :time (- (System/currentTimeMillis) time-before))
               "Request successful")
        (let [response (convert-response result {:debugging (:debugging query-opts)
                                                 :metadata  (:metadata request)
                                                 :resource  (:resource request)
                                                 :url       (:url request)
                                                 :params    (:query-params request)
                                                 :timeout   request-timeout
                                                 :time      (- (System/currentTimeMillis) time-before)})]
          ; After Request hook
          (hook/execute-hook query-opts :after-request (reduce-kv (fn [result k v]
                                                                    (if (= k :body)
                                                                      (assoc result k (json/generate-string v))
                                                                      (assoc result k v)))
                                                                  {} response))
          (go (>! output-ch response))))
      (let [error-data (assoc log-data :success false
                                       :status 408
                                       :metadata (some-> request :metadata)
                                       :url (some-> request :url)
                                       :params    (:query-params request)
                                       :time (- (System/currentTimeMillis) time-before)
                                       :errordetail (pr-str (some-> result :error)))]
        (error error-data "Request failed")
        (hook/execute-hook query-opts :after-request error-data)
        (go (>! output-ch {:status   408
                           :metadata (:metadata request)
                           :body     {:message "timeout"}}))))))

(defn make-request
  ([request query-opts]
   (let [output-ch (chan)]
     (make-request request query-opts output-ch)
     output-ch))
  ([request query-opts output-ch]
   (let [request (parse-query-params request)
         time-before (System/currentTimeMillis)
         request-timeout (if (nil? (:timeout request)) (:timeout query-opts) (:timeout request))
         forward (some-> query-opts :forward-params)
         forward-params (if (nil? forward) {} forward)
         request-map {:resource        (:resource request)
                      :timeout         request-timeout
                      :idle-timeout    (/ request-timeout 5)
                      :connect-timeout request-timeout
                      :url             (:url request)
                      :query-params    (into (:query-params request) forward-params)
                      :headers         (:headers request)
                      :time            time-before
                      :body            (:post-body request)
                      :client client}
         post-body (some-> request :post-body)]
     (debug request-map "Preparing request")
     ; Before Request hook
     (hook/execute-hook query-opts :before-request request-map)
     (if (nil? (:body request-map))
       (retry-on-error #(http/get (:url request) request-map) #(request-callback %
                                                                                 :request request
                                                                                 :request-timeout request-timeout
                                                                                 :query-opts query-opts
                                                                                 :time-before time-before
                                                                                 :output-ch output-ch) MAX_RETRIES)
       (retry-on-error #(http/post (:url request)
                                   (assoc request-map :content-type "application/json"))
                       #(request-callback %
                                          :request request
                                          :request-timeout request-timeout
                                          :query-opts query-opts
                                          :time-before time-before
                                          :output-ch output-ch) MAX_RETRIES)))))



(defn query-and-join [requests output-ch query-opts]
  (go-loop [[ch & others] (map #(make-request % query-opts) requests)
            result []]
    (if ch
      (recur others (conj result (<! ch)))
      (do
        (>! output-ch result)))))

(defn vector-with-nils? [v]
  (and (seq? v)
       (some nil? v)))

(defn failure? [requests]
  (or (nil? requests) (vector-with-nils? requests)))

(defn perform-request [url query-item-data state encoders result-ch query-opts]
  (let [requests (builder/build-requests url query-item-data encoders state)]
    (cond
      (failure? requests) (go (>! result-ch {:status nil :body nil}))
      (sequential? requests) (query-and-join requests result-ch query-opts)
      :else (make-request requests query-opts result-ch))))

(defn do-request-url [mappings query-item-data state encoders result-ch query-opts]
  (let [url (get-service-endpoint mappings (:from query-item-data))]
    (perform-request url query-item-data state encoders result-ch query-opts)))

(defn do-request-data [{[entity & path] :from} state result-ch]
  (go (>! result-ch (-> (query/find-query-item entity (:done state))
                        second
                        (update-in [:body] #(traverse % path))))))

(defn do-request [mappings {:keys [to-do state]} encoders exception-ch {:keys [debugging] :as query-opts}]
  (try+
    (let [[query-item-name query-item-data] to-do
          result-ch (chan 1 (map #(vector query-item-name %)))
          from (:from query-item-data)]
      (cond
        (keyword? from) (do-request-url mappings query-item-data state encoders result-ch query-opts)
        (vector? from) (do-request-data query-item-data state result-ch)
        (string? from) (throw+ {:type :invalid-resource-type}))
      result-ch)
    (catch [:type :invalid-resource] e
      (go (>! exception-ch e)))
    (catch [:type :expansion-error] e
      (go (>! exception-ch e)))
    (catch Object e
      (go (>! exception-ch e)))))
