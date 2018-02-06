(ns dragon.watcher.core
  (:require
    [dragon.watcher.impl.hawk :as hawk])
  (:import
    (dragon.watcher.impl.hawk HawkWatcher)))

(defprotocol Watcher
  (add-path [this path])
  (add-paths [this paths])
  (handle-event [this event]))

(extend HawkWatcher
        Watcher
        hawk/behaviour)

(defn create-watcher
  [watcher-type component]
  (case watcher-type
    :hawk (hawk/create-watcher component)))
