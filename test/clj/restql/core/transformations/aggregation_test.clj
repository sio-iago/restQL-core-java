(ns restql.core.transformations.aggregation-test
  (:require [clojure.test :refer :all]
            [restql.core.transformations.aggregation :as aggregation]))


(deftest simple-aggregation
  (let [query '((:product {:from :product :with {:id 123}})
                 (:product.price {:from :product-price :with {:productId [:product :id]}}))
        result {:product       {:result {:id 123} :details {:status 200}}
                :product.price {:result {:price 99} :details {:status 200}}}]
    (is (= (aggregation/aggregate query result) {:product       {:details {:status 200} :result {:id 123 :price {:price 99}}}
                                                 :product.price {:details {:status 200}}}))))

(deftest list-aggregation
  (let [query '((:product {:from :product :with {:id [123 345]}})
                 (:product.price {:from :product-price :with {:productId [:product :id]}}))
        result {:product       '({:result {:id 123} :details {:status 200}}
                                  {:result {:id 345} :details {:status 200}}
                                  )
                :product.price '({:result {:price 99} :details {:status 200}}
                                  {:result {:price 159} :details {:status 200}}
                                  )}]
    (is (= (aggregation/aggregate query result) {:product       [{:details {:status 200} :result {:id 123 :price {:price 99}}}
                                                                 {:details {:status 200} :result {:id 345 :price {:price 159}}}]
                                                 :product.price [{:details {:status 200}} {:details {:status 200}}]}))))

(deftest resource-does-not-exist
  (let [query '((:product {:from :product :with {:id 123}})
                 (:description.price {:from :product-price :with {:productId [:product :id]}}))
        result {:product           {:result {:id 123} :details {:status 200}}
                :description.price {:result {:price 99} :details {:status 200}}}]
    (is (= (aggregation/aggregate query result) {:product           {:details {:status 200} :result {:id 123}}
                                                 :description.price {:result {:price 99} :details {:status 200}}}))))