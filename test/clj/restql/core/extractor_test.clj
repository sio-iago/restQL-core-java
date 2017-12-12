(ns restql.core.extractor-test
  (:require [clojure.test :refer [deftest is]])
  (:use restql.core.extractor))

(deftest simple-traverse-test
  (is (=
    "123"
    (traverse {:customer {:id "123"}} [:customer :id]))))

(deftest extract-multiple-test 
  (is (=
    {:body nil :path [] :base []}
    (extract-multiple {:customer {:id "123"}} [:customer :id]))))

(deftest vector-traverse-test
  (is (=
    ["1" "2"]
    (traverse {:lines [{:id "1"} {:id "2"}]} [:lines :id]))))

(deftest vector-extract-multiple-test
  (is (=
    {:body [{:id "1"} {:id "2"}]
     :base [:lines]
     :path [:id]}

    (extract-multiple {:lines [{:id "1"} {:id "2"}]} [:lines :id]))))

(deftest complex-extract-multiple-test
  (is (=
    {:body [{:product {:id 1}}]
     :path [:product :id]
     :base [:cart :lines]}

    (extract-multiple {:cart {:lines [{:product {:id 1}}]}}
                                 [:cart :lines :product :id]))))

(deftest has-multiples-test
  (is (= false (has-multiples {:customer {:id "123"}} [:customer :id])))
  (is (= true  (has-multiples {:lines [{:id "1"} {:id "2"}]} [:lines :id])))

  (is (= true  (has-multiples [{:id "1"} {:id "2"}] [])))
  (is (= true  (has-multiples [{:id "1"} {:id "2"}] nil)))
  (is (= true  (has-multiples [{:id "1"} {:id "2"}] (seq []))))

  (is (= false (has-multiples {:id "1"} [])))
  (is (= false (has-multiples {:id "1"} nil)))
  (is (= false (has-multiples {:id "1"} (seq [])))))
