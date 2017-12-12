(ns restql.core.hooks.core
  (:require [restql.core.log :as log]
            [restql.core.log :refer [warn]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]))

(defn wrap-java-hook [java-hook]
  (fn [data]
    (try
      (let [hook-obj (.newInstance java-hook)]
        (.setData hook-obj (java.util.HashMap. (stringify-keys data)))
        (.execute hook-obj))
      (catch Exception e
        (warn "Error running hook class " (pr-str java-hook) ": " (.getMessage e))
        ""))))

(defn wrap-java-hooks [query-options]
  (if (contains? query-options :java-hooks)
    (let [hooks (into {} (query-options :java-hooks))]
      (reduce-kv (fn [result key value]
                   (assoc result (keyword key) (map wrap-java-hook value))) {} hooks))
    {}))

(defn wrap-clojure-hooks [query-options]
  (if (contains? query-options :clojure-hooks)
    (query-options :clojure-hooks)
    {}))

(defn concat-hooks [query-options]
  (into (wrap-clojure-hooks query-options)
        (wrap-java-hooks query-options)))


(defn execute-hook [query-options hook-name param-map]
  (let [hooks (concat-hooks query-options)]
    (if (contains? hooks hook-name)
      (let [hook-fns (hooks hook-name)]
        (doseq [hook-fn hook-fns]
          (hook-fn (assoc param-map :query-options query-options)))))))

(comment
  "
  This is the hook map format restQL-core understands, where:

  + hook-type-n => a clojure function
  + class-type-n => a java Class implementing the hook
  "
  hook-format {:clojure-hooks {:before-query [hook-bq-1]
                               :after-query [hook-aq-1 hook-aq-2]
                               :before-request [hook-br-1, hook-br-2]
                               :after-request [hook-ar-1 hook-ar-2 hook-ar-3]}
               :java-hooks {:before-query [class-bq-1]
                            :after-query [class-aq-1 class-aq-2]
                            :before-request [class-br-1, class-br-2]
                            :after-request [class-ar-1]}})