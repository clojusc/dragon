(ns dragon.components.config
  (:require [com.stuartsierra.component :as component]
            [dragon.config :as config]
            [taoensso.timbre :as log]))

(defrecord Config [
  builder]
  component/Lifecycle

  (start [component]
    (log/info "Starting config component ...")
    (log/debug "Started config component.")
    (let [cfg (builder)]
      (log/trace "Built configuration:" cfg)
      (assoc component :dragon cfg)))

  (stop [component]
    (log/info "Stopping config component ...")
    (log/debug "Stopped config component.")
    (assoc component :dragon nil)))

(defn create-config-component
  ""
  [config-builder-fn]
  (map->Config
    {:builder config-builder-fn}))
