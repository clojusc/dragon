(ns dragon.components.db
  (:require [com.stuartsierra.component :as component]
            [dragon.components.core :as component-api]
            [dragon.event.subscription :as subscription]
            [dragon.event.tag :as tag]
            [dragon.event.topic :as topic]
            [taoensso.timbre :as log]))

(defrecord DataBase []
  component/Lifecycle

  (start [component]
    (log/info "Starting db component ...")
    (let [a "b"]
      (log/debug "Started db component.")
      component))

  (stop [component]
    (log/info "Stopping db component ...")
    (let [a "b"]
      (log/debug "Stopped db component.")
      component)))

(defn create-db-component
  ""
  []
  (->DataBase))
