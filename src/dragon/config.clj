(ns dragon.config
  (:require
    [clojusc.config.unified.config :as config])
  (:refer-clojure :exclude [name read]))

(def config-file "config/dragon/config.edn")

(defn data
  ([]
    (data config-file))
  ([filename]
    (config/data filename)))
