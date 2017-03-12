(ns dragon.web.content
  (:require [clojusc.twig :refer [pprint]]
            [selmer.parser :as selmer]
            [taoensso.timbre :as log]))

(defn render
  [template data]
  (log/tracef "Rendering template %s with data %s" template (pprint data))
  (selmer/render-file
    template
    data))
