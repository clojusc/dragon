(ns dragon.selmer.tags.flickr
  (:require [clojure.string :as string]
            [taoensso.timbre :as log]
            [trifl.core :as util]))

(defn img-tag [args context-map]
  (log/warn "Passed args:" args)
  (log/warn "Passed context-map:" context-map))
