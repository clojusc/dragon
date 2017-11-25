(ns dragon.components.event
  (:require [com.stuartsierra.component :as component]
            [dragon.components.core :as component-api]
            [dragon.event.subscription :as subscription]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.event.topic :as topic]
            [taoensso.timbre :as log]))

(defrecord Event [pubsub]
  component/Lifecycle

  (start [component]
    (log/info "Starting event component ...")
    (log/debug "Started event component.")
    (let [dataflow (event/create-dataflow-pubsub)
          component (assoc-in component component-api/dataflow-keys dataflow)]
      (log/info "Adding subscribers ...")
      component))

  (stop [component]
    (log/info "Stopping event component ...")
    (if-let [pubsub-dataflow (get-in component component-api/dataflow-keys)]
      (event/delete pubsub-dataflow))
    (let [component (assoc-in component component-api/dataflow-keys nil)]
      (log/debug "Stopped event component.")
      component)))

(defn create-event-component
  ""
  []
  (map->Event
   {:pubsub {}}))
