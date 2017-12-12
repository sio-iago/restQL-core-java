(ns restql.core.cache
    (:require [clojure.tools.logging :as log]
              [slingshot.slingshot :refer [throw+]]
              [clojure.core.cache :as cache]
              [clojure.core.memoize :as memo]))

(def CACHED_COUNT 2000)

(defn cached
    "Verifies if a given function is cached, executing and saving on the cache
     if not cached or returning the cached value"
    [function]

    (memo/fifo function {} :fifo/threshold CACHED_COUNT))

(defn clear-cache
    "Clears the cache"
    [cached-fn]

    (memo/memo-clear! cached-fn))
