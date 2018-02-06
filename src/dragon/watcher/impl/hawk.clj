(ns dragon.watcher.impl.hawk
  (:require
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [hawk.core :as hawk]
    [taoensso.timbre :as log]
    [trifl.fs :as fs]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn clj?
  [_context event]
  (when (= :clj (fs/extension (:file event)))
    true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Watcher Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord HawkWatcher [component])

(defn handle-event
  [this event]
	(event/publish this tag/file-change event)
  (case (:kind event)
    :modify (event/publish this tag/file-modify event)
    :create (event/publish this tag/file-create event)
    :delete (event/publish this tag/file-delete event)
    (log/warn "Unhandled file system event type:" (:kind event)))
  this)

(defn add-paths
  [this paths]
  (hawk/watch! [{:context (constantly this)
                 :paths paths
                 ;:filter clj?
                 :handler handle-event}]))

(defn add-path
  [this path]
  (add-paths this [path]))

(def behaviour
  {:handle-event handle-event
   :add-path add-path
   :add-paths add-paths})

(defn create-watcher
  [component]
  (map->HawkWatcher {:component component}))
