(ns dragon.components.event
  (:require [com.stuartsierra.component :as component]
            [dragon.event.subscription :as subscription]
            [dragon.event.system.core :as event]
            [dragon.event.topic :as topic]
            [taoensso.timbre :as log]))

(defrecord Event [pubsub]
  component/Lifecycle

  (start [component]
    (log/info "Starting event component ...")
    (log/debug "Started event component.")
    (let [component (assoc component :pubsub pubsub)]
      (log/info "Adding subscribers ...")
      (subscription/subscribe-all component)
      component))

  (stop [component]
    (log/info "Stopping event component ...")
    (log/debug "Stopped event component.")
    (event/delete (:pubsub component))
    (assoc component :pubsub nil)))

(defn create-event-component
  ""
  ([]
   (create-event-component topic/dataflow-events))
  ([topic]
   (->Event
    (event/create-pubsub topic))))
