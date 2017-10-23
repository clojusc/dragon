(ns dragon.selmer.tags.flickr
  (:require [clojure.string :as string]
            [selmer.parser :as parser]
            [selmer.tags :as tags]
            [taoensso.timbre :as log]
            [trifl.core :as util]))

(defn img-tag [args context-map]
  (log/fatal "Passed args:" args)
  (log/fatal "Passed context-map:" context-map))
