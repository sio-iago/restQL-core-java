(ns restql.core.api.parallelism-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as json]
            [restql.core.api.restql :as restql]
            [stub-http.core :refer :all]
            ))

(defn hero-route []
    {:status 200 :content-type "application/json" :body (json/generate-string {:hi "I'm hero" :sidekickId ["A"] })})

(defn sidekick-route []
    {:status 200 :content-type "application/json" :body (json/generate-string {:hi "I'm sidekick"})})

(defn execute-query [baseUrl query]
    (restql/execute-query :mappings {:hero                (str baseUrl "/hero")
                                     :sidekick            (str baseUrl "/sidekick") }
                          :query query
                          )
)

; In this case if restQL makes the request to sideKick in parallel the global timeout will not trigger
(deftest chained-call
    (with-routes!
        {"/hero"     (hero-route)
         "/sidekick" (sidekick-route)}
        (let [result (execute-query uri "from hero\nfrom sidekick with id = hero.sidekickId")]
            (println result)
            (is (= 200 (get-in result [:hero :details :status])))
            ;(is (= 200 (get-in result [:sidekick :details :status])))
        )
    )
)