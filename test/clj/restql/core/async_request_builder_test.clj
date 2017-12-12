(ns restql.core.async-request-builder-test
  (:require [clojure.test :refer [deftest is]])
  (:use restql.core.async-request-builder))

(def state {:done [[:cart {:headers {:Location 1}
                           :body {:id 1
                                  :lines [{:productId "123" :sku "111"}
                                          {:productId "456" :sku "222"}]
                                  :offers [{:id "123111"}
                                           {:id "456222"}]
                                  :headers {:Location "Location Field"}}}]

                   [:blobs {:body [{:id "999"}
                                   {:id "888"}]}]
                   [:blebs [{:body {:id "aaa"}}
                            {:body {:id "bbb"}}]]]})


(deftest replace-simple-path-with-value
  (is (=
        {:productId "123"}
        (replace-path-with-value {:productId [:product :id]} [[:product :id]] ["123"]))))

(deftest replace-complex-path-with-value
  (is (=
        {:productId "123" :sku "456" :data {:bar "foo"}}
        (replace-path-with-value
          {:productId [:product :id] :sku [:product :lines :sku] :data {:bar "foo"}}
          [[:product :id] [:product :lines :sku]]
          ["123" "456"]))))

(deftest should-not-replace-any-key-value
  (is (=
        {:productId "123" :sku "456" :test [:foo :bar]}
        (replace-path-with-value
          {:productId "123" :sku "456" :test [:foo :bar]}
          [[:foo :baz]]
          ["lalaland"]))))

(deftest build-requests-test
  (is (=
    [{:url "http://example/123"
      :query-params {:sku "111"  :offer "123111"}
      :resource :example
      :metadata nil
      :timeout nil
      :headers nil
      :post-body nil}
      {:url "http://example/456"
      :resource :example
      :metadata nil
      :query-params {:sku "222" :offer "456222"}
      :timeout nil
      :headers nil
       :post-body nil}]
    
    (build-requests "http://example/:id"
                    {:from :example
                      :with {:id [:cart :lines :productId]
                             :sku [:cart :lines :sku]
                             :offer [:cart :offers :id]}}
                    {}
                    state))))

(deftest build-requests-with-list-of-params
  (is (=
        [{:url "http://example/123"
          :query-params {:name "ABC"}
          :resource :example
          :metadata nil
          :timeout nil
          :headers nil
          :post-body nil}
         {:url "http://example/456"
          :resource :example
          :metadata nil
          :query-params {:name "DEF"}
          :timeout nil
          :headers nil
          :post-body nil}]

        (build-requests "http://example/:id"
                        {:from :example
                         :with {:id ["123" "456"]
                                :name ["ABC" "DEF"]}}
                        {}
                        state))))


(deftest search-multiple-entities-test
  (is (=
    #{[{:productId "123" :sku "111"}
       {:productId "456" :sku "222"}]}

    (get-multiple-entities {:from :blibs
                            :with {:product [:cart :lines :productId]
                                   :sku     [:cart :lines :sku]}} state))))

(deftest create-request-with-headers-test
  (is (=
    {:url "http://localhost:9999"
     :query-params {:id "123"}
     :resource :cart
     :metadata nil
     :timeout nil
     :headers {"tid" "aaaaaaaaaa"}
     :post-body nil}
    
    (build-request "http://localhost:9999" 
                   {:from :cart 
                    :with {:id "123"} 
                    :with-headers {"tid" "aaaaaaaaaa"}} 
                   {}
                   {:done []}))))

(deftest create-request-without-headers-test
  (is (=
    {:url "http://localhost:9999"
     :query-params {:id "123"}
     :metadata nil
     :resource :cart
     :timeout nil
     :headers nil
     :post-body nil}
    
    (build-request "http://localhost:9999" 
                   {:from :cart 
                    :with {:id "123"}}
                   {}
                   {:done []}))))

(deftest retrieving-value-from-state-test
  (is (=
    1
    (get-reference-from-state [:cart :id] state)))

  (is (=
    1
    (get-reference-from-state [:cart :headers "Location"] state)))

  (is (=
    "Location Field"
    (get-reference-from-state [:cart :headers :Location] state))))

(deftest no-multiple-requests-test
  (is (=
    #{}
    (get-multiple-requests {:with {:id [:cart :id]}} state)))

  (is (=
    #{}
    (get-multiple-paths {:with {:id [:cart :id]}} state))))


(deftest simple-multiple-request-test
  (is (=
    #{[:blobs :id]}
    (get-multiple-paths {:with {:id [:blobs :id]}} state)))

  (is (=
    #{:blobs}
    (get-multiple-requests {:with {:id [:blobs :id]}} state))))

(deftest derived-multiple-request-test
  (is (=
    #{[:blebs :id]}
    (get-multiple-paths {:with {:id [:blebs :id]}} state)))

  (is (=
    #{:blebs}
    (get-multiple-requests {:with {:id [:blebs :id]}} state))))

(deftest nested-multiple-request-test
  (is (=
    #{[:cart :lines :productId]}
    (get-multiple-paths {:with {:id [:cart :lines :productId]}} state)))

  (is (=
    #{:cart}
    (get-multiple-requests {:with {:id [:cart :lines :productId]}} state))))

(deftest nested-complex-multiple-request-test
  (is (=
    #{:cart}
    (get-multiple-requests {:with {:id {:field [:cart :lines :productId]}}} 
                           state))))

(deftest search-of-multiple-paths-test
  (is (=
    #{[:cart :lines :productId]}
    (get-multiple-paths {:with {:id {:field [:cart :lines :productId]}}}
                        state))))

(deftest create-request-with-custom-timeout-test
  (is (=
    {:url "http://localhost:9999"
     :query-params {:id "123"}
     :resource :cart
     :metadata nil
     :timeout 2000
     :headers nil
     :post-body nil}
    
    (build-request "http://localhost:9999" 
                   {:from :cart :timeout 2000 :with {:id "123"}}
                   {}
                   {:done []}))))


(deftest create-request-with-post-body
  (is (=
        {:url "http://localhost:9999"
         :query-params {:id "123"}
         :resource :cart
         :metadata nil
         :timeout nil
         :headers nil
         :post-body "{\"jedi\":{\"name\":\"Luke Skywalker\",\"weaponId\":1}}"}

        (build-request "http://localhost:9999"
                       {:from :cart
                        :with {:id "123"}
                        :with-body {:jedi {:name "Luke Skywalker" :weaponId 1}}}
                       {}
                       {:done []}))))


(deftest produce-nil-when-chaining-fails
  (is (nil?
        (build-request "http://foo.url/:id"
                       {:from :foo :with {:id [:bar :fooid]}}
                       {}
                       {:done [[:bar {:status 404
                                      :headers {}
                                      :url "http://bar.url/123"
                                      :timeout 1000
                                      :params {}
                                      :response-time 23
                                      :body {:errorCode "404"}}]]
                        :requested []
                        :to-do [[:foo {:from :foo :with {:id [:bar :fooid]}}]]}))))