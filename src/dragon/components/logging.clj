(ns dragon.components.logging
  (:require [clojusc.twig :as logger]
            [com.stuartsierra.component :as component]
            [dragon.components.config :as config]
            [taoensso.timbre :as log]))

(defrecord Logging []
  component/Lifecycle

  (start [component]
    (log/info "Starting logging component ...")
    (let [log-level (config/log-level component)
          log-nss (vec (config/log-nss component))]
      (log/debug "Setting up logging with level" log-level)
      (log/debug "Logging namespaces:" log-nss)
      (logger/set-level! log-nss log-level)
      (log/debug "Started logging component.")
      component))

  (stop [component]
    (log/info "Stopping logging component ...")
    (log/debug "Stopped logging component.")
    component))

(defn create-logging-component
  ""
  []
  (->Logging))
