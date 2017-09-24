(ns dragon.components.event
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]))

(defrecord Event []
  component/Lifecycle

  (start [component]
    (log/info "Starting event component ...")
    (log/debug "Started event component.")
    component)

  (stop [component]
    (log/info "Stopping event component ...")
    (log/debug "Stopped event component.")
    component))

(defn create-event-component
  ""
  []
  (->Event))
