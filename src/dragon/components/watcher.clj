(ns dragon.components.watcher
  (:require
    [dragon.components.config :as config]
    [dragon.components.event :as event]
    [dragon.watcher.core :as watcher]
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Watcher Component API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Lifecycle Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord Watcher [])

(defn start
  [this]
  (log/info "Starting watcher component ...")
  (let [watch (watcher/create-watcher (config/watcher-type this) this)
        content-paths (config/watcher-content-dirs this)]
    ;; XXX track watches and save in component
    (log/debug "Adding content paths to watch:" content-paths)
    (watcher/add-paths watch content-paths)
    (log/debug "Started watcher component."))
  this)

(defn stop
  [this]
  (log/info "Stopping watcher component ...")
  (log/debug "Stopped watcher component.")
  this)

(def lifecycle-behaviour
  {:start start
   :stop stop})

(extend Watcher
  component/Lifecycle
  lifecycle-behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-component
  ""
  []
  (->Watcher))
