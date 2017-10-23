(ns dragon.selmer.tags.core
  (:require [dragon.selmer.tags.flickr :as flickr]
            [selmer.parser :as parser]
            [selmer.tags :as tags]
            [taoensso.timbre :as log]))

(defn register!
  []
  (parser/add-tag! :flickr-img flickr/img-tag)
  (parser/add-tag! :foo
    (fn [args context-map]
      (str "foo " (first args)))))
