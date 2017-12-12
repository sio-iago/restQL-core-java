(ns restql.parser.core-test
  (:require [clojure.test :refer :all]
            [restql.parser.core :refer :all]
            [clojure.tools.reader :as edn]))

(deftest testing-edn-string-production

  (testing "Testing simple query"
    (is (= (parse-query "from heroes as hero")
           [:hero {:from :heroes}])))

  (testing "Testing simple query without alias"
    (is (= (parse-query "from heroes")
           [:heroes {:from :heroes}])))

  (testing "Testing simple query with-body json"
    (is (= (parse-query "from heroes
                                        body
                                          foo = \"bar\"")
           [:heroes {:from :heroes :with-body {:foo "bar"}}])))

  (testing "Testing simple query with-body complex json"
    (is (= (parse-query "from heroes
                                        body
                                          foo = \"bar\",
                                          bar = {
                                            baz: \"baz\"
                                          }")
           [:heroes {:from :heroes :with-body {:foo "bar" :bar {:baz "baz"}}}])))

  (testing "Testing simple query with-body complex json and chaining"
    (is (= (parse-query "from heroes
                                        body
                                          id = api.id,
                                          bar = {
                                            baz: \"baz\"
                                          }")
           [:heroes {:from :heroes :with-body {:id [:api :id] :bar {:baz "baz"}}}])))

  (testing "Testing simple query params a use clause"
    (is (= (parse-query "use cache-control = 900
                                      from heroes as hero")
           ^{:cache-control 900} [:hero {:from :heroes}])))

  (testing "Testing simple query params ignore errors"
    (is (= (parse-query "from heroes as hero ignore-errors")
           [:hero ^{:ignore-errors "ignore"} {:from :heroes}])))

  (testing "Testing multiple query"
    (is (= (parse-query "from heroes as hero
                                      from monsters as monster")
           [:hero {:from :heroes}
            :monster {:from :monsters}])))

  (testing "Testing query params one numeric parameter"
    (is (= (parse-query "from heroes as hero params id = 123")
           [:hero {:from :heroes :with {:id 123}}])))

  (testing "Testing query params one string parameter"
    (is (= (parse-query "from heroes as hero params id = \"123\"")
           [:hero {:from :heroes :with {:id "123"}}])))

  (testing "Testing query params variable parameter"
    (is (= (parse-query "from heroes as hero params id = $id" :context {"id" "123"})
           [:hero {:from :heroes :with {:id "123"}}])))

  (testing "Testing query params one null parameter"
    (is (= (parse-query "from heroes as hero params id = 123, spell = null")
           [:hero {:from :heroes :with {:id 123 :spell nil}}])))

  (testing "Testing query params one boolean parameter"
    (is (= (parse-query "from heroes as hero params magician = true")
           [:hero {:from :heroes :with {:magician true}}])))

  (testing "Testing query params one array parameter"
    (is (= (parse-query "from heroes as hero params class = [\"warrior\", \"magician\"]")
           [:hero {:from :heroes :with {:class ["warrior" "magician"]}}])))

  (testing "Testing query params one complex parameter"
    (is (= (parse-query "from heroes as hero params equip = {sword: 1, shield: 2}")
           [:hero {:from :heroes :with {:equip {:sword 1 :shield 2}}}])))

  (testing "Testing query params one complex parameter params subitems"
    (is (= (parse-query "from heroes as hero params equip = {sword: {foo: \"bar\"}, shield: [1, 2, 3]}")
           [:hero {:from :heroes :with {:equip {:sword {:foo "bar"} :shield [1 2 3]}}}])))

  (testing "Testing query params one chained parameter"
    (is (= (parse-query "from heroes as hero params id = player.id")
           [:hero {:from :heroes :with {:id [:player :id]}}])))

  (testing "Testing query params one chained parameter and metadata"
    (is (= (parse-query "from heroes as hero params id = player.id -> json")
           [:hero {:from :heroes :with {:id ^{:encoder :json} [:player :id]}}])))

  (testing "Testing query params one chained parameter and metadata"
    (is (= (pr-str (parse-query "from heroes as hero params id = player.id -> encoder(\"json\", \"pretty\")"))
           (pr-str [:hero {:from :heroes :with {:id ^{:encoder :json :args ["pretty"]} [:player :id]}}]))))

  (testing "Testing query params headers"
    (is (= (parse-query "from heroes as hero headers Content-Type = \"application/json\" params id = 123")
           [:hero {:from :heroes :with-headers {"Content-Type" "application/json"} :with {:id 123}}])))

  (testing "Testing query params headers and parameters"
    (is (= (parse-query "from heroes as hero headers Authorization = $auth params id = 123" :context {"auth" "abc123"})
           [:hero {:from :heroes :with-headers {"Authorization" "abc123"} :with {:id 123}}])))

  (testing "Testing query params hidden selection"
    (is (= (parse-query "from heroes as hero params id = 1 hidden")
           [:hero {:from :heroes :with {:id 1} :select :none}])))

  (testing "Testing query params only selection"
    (is (= (parse-query "from heroes as hero params id = 1 only id, name")
           [:hero {:from :heroes :with {:id 1} :select #{:id :name}}])))

  (testing "Testing query params only selection of inner elements"
    (is (= (parse-query "from heroes as hero params id = 1 only skills.id, skills.name, name")
           [:hero {:from :heroes :with {:id 1} :select #{:name [:skills #{:id :name}]}}])))

  (testing "Testing query params paramater params dot and chaining"
    (is (= (parse-query "from heroes as hero params weapon.id = weapon.id")
           [:hero {:from :heroes :with {:weapon.id [:weapon :id]}}])))

  (testing "Testing query params only selection and a filter"
    (is (= (parse-query "from heroes as hero params id = 1 only id, name -> matches(\"foo\")")
           [:hero {:from :heroes :with {:id 1} :select #{:id [:name {:matches "foo"}]}}])))

  (testing "Testing query params only selection and a filter params wildcard"
    (is (= (parse-query "from heroes as hero params id = 1 only id -> equals(1), *")
           [:hero {:from :heroes :with {:id 1} :select #{[:id {:equals 1}] :*}}])))

  (testing "Testing filter with variable"
    (is (= (parse-query "from heroes as hero params id = 1 only id, name -> matches($name)" :context {"name" "Hero"})
           [:hero {:from :heroes :with {:id 1} :select #{:id [:name {:matches "Hero"}]}}])))

  (testing "Testing full featured query"
    (binding [*print-meta* true]
      (is (= (pr-str (parse-query "from product as products
                                                 headers
                                                     content-type = \"application/json\"
                                                 with
                                                     limit = product.id -> flatten -> json
                                                     fields = [\"rating\", \"tags\", \"images\", \"groups\"]
                                                 only 
                                                     id, name, cep, phone"))
             (pr-str [:products {:from         :product
                                 :with-headers {"content-type" "application/json"}
                                 :with         {:limit  ^{:expand false :encoder :json}
                                                        [:product :id]
                                                :fields ["rating" "tags" "images" "groups"]}
                                 :select       #{:id :name :cep :phone}}]))))))

