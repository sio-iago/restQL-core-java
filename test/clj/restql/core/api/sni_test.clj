(ns restql.core.api.sni-test
  (:require [clojure.test :refer :all]
            [restql.core.api.restql :as restql]))

(defn query [query]
    (restql/execute-query :mappings {:sni "https://3hico1lla1.execute-api.us-east-1.amazonaws.com/production/resource-status" }
                          :query query)
)


(deftest simple-request
    (let [result (query "from sni")]
            (is (= 200 (get-in result [:sni :details :status])))
    )
)