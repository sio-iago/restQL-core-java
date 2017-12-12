(ns restql.core.transformations.select
  (:require [restql.core.transformations.filters :as filters]))

(defn select-keyword [select-param entity-value]
  (get entity-value select-param))

(declare do-selection)

(defn apply-filters [filter-data entity-value]
  (if (seq filter-data)
    (filters/apply-filters entity-value filter-data)
    entity-value))

(defn select-expression [[select-key & select-params] entity-value]
  (let [entity-item (get entity-value select-key)
        subselect (->> select-params (filter set?) first)
        filters (filter map? select-params)
        filtered-items (apply-filters filters entity-item)]
    (if (nil? subselect)
      filtered-items
      (do-selection subselect filtered-items))))

(defn contains-wildcard? [select-params]
  (if (set? select-params)
    (some #(= :* %) select-params)
    false))

(defn select-single [select-params entity-value]
  (reduce (fn [result select-param]
            (cond
              (= :* select-param) result
              (keyword? select-param) (assoc result select-param (select-keyword select-param entity-value))
              (vector? select-param) (assoc result (first select-param) (select-expression select-param entity-value))
              :else result))
          (if (contains-wildcard? select-params) entity-value {})
          select-params))

(defn do-selection [select-params entity-value]
  (if (sequential? entity-value)
    (map #(select-single select-params %) entity-value)
    (select-single select-params entity-value)))

(defn do-selection-with-details [select-params entity-value]
  (if (sequential? entity-value)
    (map (partial do-selection-with-details select-params) entity-value)
    {:details (:details entity-value)
     :result (do-selection select-params (:result entity-value))}))

(defn reduce-with [query]
  (fn [acc entity value]
    (let [select-params (:select (entity query))]
      (cond
        (set? select-params) (assoc acc entity (do-selection-with-details select-params value))
        (= :none select-params) acc
        :else (assoc acc entity value)))))

(defn select [query result]
  (let [query-map (apply hash-map query)]
    (reduce-kv (reduce-with query-map) {} result)))
