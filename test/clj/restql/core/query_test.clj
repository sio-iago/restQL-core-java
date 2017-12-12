(ns restql.core.query-test
  (:require [clojure.test :refer [deftest is]])
  (:use restql.core.query))

(deftest extract-simple-dependency-test
  (is (=
    #{:checkout}
    (get-dependencies [:cart {:from :cart
                                  :with {:opn "123"
                                         :id [:checkout :cartId]}}])))

  (is (=
    #{}
    (get-dependencies [:cart {:from :cart
                                  :with {:id "123"}}])))

  (is (=
    #{:cart}
    (get-dependencies [:lines {:from [:cart :lines]}])))

  (is (=
    #{:cart :checkout}
    (get-dependencies [:ex {:from :example
                            :with {:data {:json {:a [:cart :id]
                                                 :b [:checkout :id]
                                                 :c 3}}}}]))))

(deftest extract-simple-dependency-paths
  (is (=
    #{[:checkout :cartId]}
    (get-dependency-paths {:from :cart
                               :with {:opn "123"
                                      :id [:checkout :cartId]}})))

  (is (=
    #{}
    (get-dependency-paths {:from :cart
                               :with {:id "123"}})))

  (is (=
    #{[:cart :lines]}
    (get-dependency-paths {:from [:cart :lines]})))

  (is (=
    #{[:cart :id] [:checkout :id]}
    (get-dependency-paths {:from :example
                           :with {:data {:json {:a [:cart :id]
                                                :b [:checkout :id]
                                                :c 3}}}}))))
