(ns restql.core.encoders.core-test
  (:require [slingshot.slingshot :refer [try+]]
            [clojure.test :refer [deftest is]])
  (:use restql.core.encoders.core))

(deftest test-simple-values
  (is (= "10"   (encode base-encoders 10)))
  (is (= "true" (encode base-encoders true)))
  (is (nil? (encode base-encoders nil))))
  

(deftest test-throws-exception-with-unrecognized-encoding
  (is (= 
    :ok 
    (try+
      (encode {} {:bla 123})
      (catch [:type :unrecognized-encoding] e :ok)
      (catch Object e e)))))

