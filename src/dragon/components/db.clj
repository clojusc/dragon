(ns dragon.components.db
  (:require
    [com.stuartsierra.component :as component]
    [dragon.components.config :as config]
    [dragon.data.core :as data]
    [dragon.data.sources.core :as data-source]
    [dragon.event.subscription :as subscription]
    [dragon.event.tag :as tag]
    [dragon.event.topic :as topic]
    [dragon.util :as util]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   General DB Component Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-setup-tasks
  [connector]
  (log/debug "Starting db setup tasks ...")
  (data-source/setup-schemas connector)
  (data-source/setup-subscribers connector)
  (log/debug "Finished db setup tasks."))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Components   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord DataBase [
  connector
  querier]
  component/Lifecycle

  (start [component]
    (log/info "Starting db component ...")
    (log/trace "component keys:" (keys component))
    (log/debug "Database component backend is" (config/db-type component))
    (let [connector (data-source/new-connector component)
          _ (data-source/start-db! connector)
          component (data-source/add-connection connector)
          querier (data-source/new-querier component)]
      (run-setup-tasks connector)
      (reset! (:connector component) connector)
      (reset! (:querier component) querier)
      (assoc component :connector connector
                       :querier querier)))

  (stop [component]
    (log/info "Stopping db component ...")
    (log/trace "component keys:" (keys component))
    (log/trace "config:" (:config component))
    (let [raw-connector (:connector component)
          connector (if (util/atom? raw-connector)
                      @raw-connector
                      raw-connector)]
      (data-source/stop-db! connector)
      (log/info "Stopped db component.")
      (assoc component :connector nil
                       :querier nil))))

(defn create-db-component
  ""
  []
  (->DataBase
    (atom {})
    (atom {})))
