(ns dragon.selmer.tags.core
  (:require [dragon.selmer.tags.demo :as demo]
            [dragon.selmer.tags.flickr :as flickr]
            [dragon.selmer.tags.panel :as panel]
            [selmer.parser :as parser]
            [taoensso.timbre :as log]))

(defn register!
  []
  (parser/add-tag! :flickr-img flickr/img-tag)
  (parser/add-tag! :foo demo/foo-tag)
  (parser/add-tag! :panel panel/tag :end-panel)
  (parser/add-tag! :panel-body panel/body-tag :end-panel-body)
  (parser/add-tag! :panel-body-no-pad panel/body-no-pad-tag :end-panel-body)
  (parser/add-tag! :panel-table panel/table-tag :end-panel-table)
  (parser/add-tag! :panel-row panel/row-tag :end-panel-row)
  (parser/add-tag! :panel-img panel/img-tag))
