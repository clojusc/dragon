(ns dragon.selmer.tags.flickr
  (:require [clojure.string :as string]
            [clojure.walk :as walk]
            [taoensso.timbre :as log]
            [trifl.core :as util]))

(def str-keys->kwd-keys
  {":photo-id" :photo-id
   ":album-id" :album-id
   ":height" :height
   ":width" :width})

(defn args->map
  [args]
  (->> args
       (partition 2)
       (map vec)
       (into {})
       (walk/postwalk-replace str-keys->kwd-keys)))

(defn make-api-call
  []
  )

(defn img-tag [raw-args context-map]
  (let [args (args->map raw-args)]
    (log/warn "Parsed args:" args)
    (format (str "<a href=\"\">"
                 "<img src=\"https://www.flickr.com/photos/forgottenroadsmx/%s/in/%s\">")
            (:photo-id args)
            (:album-id args))))
