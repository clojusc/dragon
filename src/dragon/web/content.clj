(ns dragon.web.content
  (:require [selmer.parser :as selmer]))

(defn render
  ([template]
    (render template identity))
  ([template func]
    (render template func {}))
  ([template func request]
    (selmer/render-file
      template
      (func request))))
