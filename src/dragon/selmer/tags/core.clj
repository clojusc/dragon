(ns dragon.selmer.tags.core
  (:require [dragon.selmer.tags.demo :as demo]
            [dragon.selmer.tags.flickr :as flickr]
            [selmer.parser :as parser]
            [taoensso.timbre :as log]))

(defn register!
  []
  (parser/add-tag! :flickr-img flickr/img-tag)
  (parser/add-tag! :foo demo/foo-tag))
