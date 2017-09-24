(ns dragon.components.config
  (:require [com.stuartsierra.component :as component]
            [dragon.config :as config]
            [taoensso.timbre :as log]))

(defrecord Config []
  component/Lifecycle

  (start [component]
    (log/info "Starting config component ...")
    (log/debug "Started config component.")
    (assoc component :dragon (config/build)))

  (stop [component]
    (log/info "Stopping config component ...")
    (log/debug "Stopped config component.")
    (assoc component :dragon nil)))

(defn create-config-component
  ""
  []
  (->Config))
