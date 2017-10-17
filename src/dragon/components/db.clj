(ns dragon.components.db
  (:require [com.stuartsierra.component :as component]
            [dragon.config.core :as config]
            [dragon.data.core :as data]
            [dragon.data.sources.core :as data-source]
            [dragon.event.subscription :as subscription]
            [dragon.event.tag :as tag]
            [dragon.event.topic :as topic]
            [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   General DB Component Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-setup-tasks
  [connector]
  (log/debug "Starting setup tasks ...")
  (data-source/setup-schemas connector)
  (data-source/setup-subscribers connector)
  (log/debug "Finished setup tasks."))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Components   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord DataBase []
  component/Lifecycle

  (start [component]
    (log/info "Starting db component ...")
    (log/debug "Database component backend is" (config/db-type component))
    (let [connector (data-source/new-connector component)
          _ (data-source/start-db! connector)
          component (data-source/add-connection connector)]
      (run-setup-tasks connector)
      (assoc component :connector connector
                       :querier (data-source/new-querier component))))

  (stop [component]
    (log/info "Stopping db component ...")
    (let [connector (:connector component)]
      (log/debug "Stopped db component.")
      (data-source/stop-db! connector)
      (-> connector
          (data-source/remove-connection)
          (assoc :querier nil)))))

(defn create-db-component
  ""
  []
  (->DataBase))
