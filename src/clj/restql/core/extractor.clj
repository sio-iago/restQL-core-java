(ns restql.core.extractor)

(defn traverse [data [map-key & path]]
  (if map-key
    (if (sequential? data)
      (recur (map map-key (flatten data)) path)
      (recur (map-key data) path))
    data))

(defn has-multiples [data [map-key & path]]
  (if map-key
    (if (sequential? data)
      true
      (recur ((keyword map-key) data) path))
    (sequential? data)))

(defn extract-multiple [data fullpath]
  (loop [multiple nil
         remain []
         base []
         data data
         [map-key & path :as partialpath] fullpath]
    (if map-key
      (if (sequential? data)
        (recur (if (nil? multiple) data multiple)
               (if (nil? multiple) (into [] partialpath) remain)
               base
               (map map-key data)
               path)
        (recur multiple remain (conj base map-key) (map-key data) path))
      {:body multiple
       :path remain
       :base (if (nil? multiple) [] base)})))
