(ns dragon.components.db
  (:require [com.stuartsierra.component :as component]
            [dragon.components.core :as component-api]
            [dragon.event.subscription :as subscription]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.event.topic :as topic]
            [taoensso.timbre :as log]))

(defrecord DataBase []
  component/Lifecycle

  (start [component]
    (log/info "Starting db component ...")
    (log/debug "Started db component.")
    (let [dataflow (event/create-dataflow-pubsub)
          component (assoc-in component component-api/dataflow-keys dataflow)]
      component))

  (stop [component]
    (log/info "Stopping db component ...")
    (event/delete (get-in component component-api/dataflow-keys))
    (let [component (assoc-in component component-api/dataflow-keys nil)]
      (log/debug "Stopped db component.")
      component)))

(defn create-db-component
  ""
  []
  (->DataBase))
