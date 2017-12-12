(ns restql.core.api.restql-test
  (:require [clojure.test :refer [deftest is]]
            [restql.core.api.restql :as restql]
            [cheshire.core :as json]
            [stub-http.core :refer :all]
            )
  )

(defn hero-route []
  {:status 200 :content-type "application/json" :body (json/generate-string {:hi "I'm hero" :sidekickId "A20"})})

(defn sidekick-route []
  {:status 200 :content-type "application/json" :body (json/generate-string {:hi "I'm sidekick"})})

(defn product-route [product-id]
  {:status 200 :content-type "application/json" :body (json/generate-string {:id product-id})})

(defn price-route [product-id price]
  {:status 200 :content-type "application/json" :body (json/generate-string {:product_id product-id
                                                                             :price      price})})
(defn description-route [product-id description]
  {:status 200 :content-type "application/json" :body (json/generate-string {:product_id  product-id
                                                                             :description description})})
(defn execute-query [baseUrl query]
  (restql/execute-query :mappings {:hero                (str baseUrl "/hero")
                                   :sidekick            (str baseUrl "/sidekick")
                                   :product             (str baseUrl "/product/:id")
                                   :product-price       (str baseUrl "/price/:productId")
                                   :product-description (str baseUrl "/description/:productId")}
                        :query query))

(deftest simple-request
  (with-routes!
    {"/hero" (hero-route)}
    (let [result (execute-query uri "from hero")]
      (is (= 200 (get-in result [:hero :details :status])))
      (is (= "I'm hero" (get-in result [:hero :result :hi])))
      )
    )
  )

;(deftest error-request-should-throw-exception
; (with-routes!
;  {"/hero" (assoc (hero-route) :status 500)}
; (is (thrown? Exception (execute-query uri "from hero")))
;)
;)

(deftest unmapped-resource-should-throw-exception
  (is (thrown? Exception (execute-query "http://any" "from villain")))
  )

(deftest error-request-with-ignore-errors-shouldnt-throw-exception
  (with-routes!
    {"/hero" (assoc (hero-route) :status 500)}
    (let [result (execute-query uri "from hero ignore-errors")]
      (is (= 500 (get-in result [:hero :details :status])))
      )
    )
  )

(deftest timeout-request-shoudl-return-408
  (with-routes!
    {"/hero" (assoc (hero-route) :delay 200)}
    (let [result (execute-query uri "from hero timeout 100")]
      (is (= 408 (get-in result [:hero :details :status])))
      )
    )
  )

;(deftest unreachable-resource-should-return-503
; (let [result (execute-query "http://localhost:9999" "from hero ignore-errors")]
;  (is (= 503 (get-in result [:hero :details :status])))
;)
;)

(deftest chained-call
  (with-routes!
    {"/hero"     (hero-route)
     "/sidekick" (sidekick-route)}
    (let [result (execute-query uri "from hero\nfrom sidekick")]
      (is (= 200 (get-in result [:hero :details :status])))
      (is (= 200 (get-in result [:sidekick :details :status])))
      )
    )
  )

;(deftest should-throw-exeption-if-chainned-resource-fails
; (with-routes!
;{"/hero" (hero-route)
; "/sidekick" (sidekick-route)}
;(is (thrown? Exception (execute-query uri "from hero\nfrom sidekick with id = hero.sidekickId")))
;)
;)

(deftest shouldnt-throw-exeption-if-chainned-resource-timeout-and-ignore-error
  (with-routes!
    {"/hero" (hero-route)}
    {"/sideck" (assoc (sidekick-route) :delay 200)}
    (let [result (execute-query uri "from hero\nfrom sidekick timeout 100 with id = hero.sidekickId ignore-errors")]
      (is (= 200 (get-in result [:hero :details :status])))
      (is (= 408 (get-in result [:sidekick :details :status])))
      )
    )
  )

(deftest response-simple-aggregation
  (with-routes!
    {"/price/123"   (price-route 123 99.99M)
     "/product/123" (product-route 123)}
    (let [result (execute-query uri "from product with id = 123 \nfrom product-price as product.price with productId = product.id")]
      (is (= 99.99 (get-in result [:product :result :price :price])))
      (is (nil? (get-in result [:product.price :result]))))))

(deftest response-simple-aggregation-with-filter
  (with-routes!
    {"/price/123"   (price-route 123 99.99M)
     "/product/123" (product-route 123)}
    (let [result (execute-query uri "from product with id = 123 \nfrom product-price as product.price with productId = product.id only price")]
      (is (= 99.99 (get-in result [:product :result :price :price])))
      (is (nil? (get-in result [:product.price :result]))))))

(deftest response-simple-aggregation-with-filter-on-aggregated-field
  (with-routes!
    {"/price/123"   (price-route 123 99.99M)
     "/product/123" (product-route 123)}
    (let [result (execute-query uri "from product with id = 123 \nfrom product-price as product.price with productId = product.id only product_id")]
      (is (nil? (get-in result [:product :result :price :price])))
      (is (nil? (get-in result [:product.price :result]))))))

(deftest response-list-aggregation
  (with-routes!
    {"/price/123"   (price-route 123 99.99M)
     "/price/345"   (price-route 345 15.99)
     "/product/123" (product-route 123)
     "/product/345" (product-route 345)}
    (let [result (execute-query uri "from product with id = [123, 345] \nfrom product-price as product.price with productId = product.id")
          product-result (:product result)
          product-price-result (:product.price result)]
      (is (= 99.99 (:price (:price (:result (first product-result))))))
      (is (= 15.99 (:price (:price (:result (second product-result))))))
      (is (nil? (:result (first product-price-result))))
      (is (nil? (:result (second product-price-result)))))))

(deftest response-multiple-aggregations
  (with-routes!
    {"/price/123"       (price-route 123 99.99M)
     "/product/123"     (product-route 123)
     "/description/123" (description-route 123 "an awesome product!")}
    (let [result (execute-query uri "from product with id = 123 \nfrom product-price as product.price with productId = product.id \n from product-description as product.description with productId = product.id")]
      (is (= 99.99 (get-in result [:product :result :price :price])))
      (is (= "an awesome product!" (get-in result [:product :result :description :description])))
      (is (nil? (get-in result [:product.price :result])))
      (is (nil? (get-in result [:product.description :result]))))))

(deftest response-multiple-list-aggregations
  (with-routes!
    {"/price/123"       (price-route 123 99.99M)
     "/price/345"       (price-route 345 15.99)
     "/product/123"     (product-route 123)
     "/product/345"     (product-route 345)
     "/description/123" (description-route 123 "an awesome product!")
     "/description/345" (description-route 123 "another awesome product!")}
    (let [result (execute-query uri "from product with id = [123, 345] \nfrom product-price as product.price with productId = product.id \n from product-description as product.description with productId = product.id")
          product-result (:product result)
          product-price-result (:product.price result)
          product-description-result (:product.description result)]
      (is (= 99.99 (:price (:price (:result (first product-result))))))
      (is (= "an awesome product!" (:description (:description (:result (first product-result))))))
      (is (= 15.99 (:price (:price (:result (second product-result))))))
      (is (= "another awesome product!" (:description (:description (:result (second product-result))))))
      (is (nil? (:result (first product-price-result))))
      (is (nil? (:result (second product-price-result))))
      (is (nil? (:result (first product-description-result))))
      (is (nil? (:result (second product-description-result)))))))