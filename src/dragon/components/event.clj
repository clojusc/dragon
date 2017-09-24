(ns dragon.components.event
  (:require [com.stuartsierra.component :as component]
            [dragon.event.names :as names]
            [dragon.event.subscription :as subscription]
            [dragon.event.system.core :as event]
            [taoensso.timbre :as log]))

(defrecord Event [pubsub]
  component/Lifecycle

  (start [component]
    (log/info "Starting event component ...")
    (log/debug "Started event component.")
    (let [component (assoc component :pubsub pubsub)]
      (log/info "Adding subscribers ...")
      (subscription/subscribe-all {:event component})
      component))

  (stop [component]
    (log/info "Stopping event component ...")
    (log/debug "Stopped event component.")
    (event/delete (:pubsub component))
    (assoc component :pubsub nil)))

(defn create-event-component
  ""
  ([]
   (create-event-component names/dataflow-events))
  ([topic]
   (->Event
    (event/create-pubsub topic))))
