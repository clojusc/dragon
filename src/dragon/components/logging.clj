(ns dragon.components.logging
  (:require [clojusc.twig :as logger]
            [com.stuartsierra.component :as component]
            [dragon.config :as config]
            [taoensso.timbre :as log]))

(defrecord Logging []
  component/Lifecycle

  (start [component]
    (log/info "Starting logging component ...")
    (logger/set-level!
     (config/log-nss component)
     (config/log-level component))
    (log/debug "Started logging component.")
    component)

  (stop [component]
    (log/info "Stopping logging component ...")
    (log/debug "Stopped logging component.")
    component))

(defn create-logging-component
  ""
  []
  (->Logging))
