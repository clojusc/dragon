(ns dragon.components.db
  (:require [com.stuartsierra.component :as component]
            [dragon.config :as config]
            [dragon.data.core :as data]
            [dragon.data.sources.core :as db-core]
            [dragon.data.sources.datomic :as datomic]
            [dragon.data.sources.redis :as redis]
            [dragon.event.subscription :as subscription]
            [dragon.event.tag :as tag]
            [dragon.event.topic :as topic]
            [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   General DB Component Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX Use protocols below & ditch the quick-fix; see ticket #17

(defn start-db!
  [component]
  (case (config/db-type component)
    :datomic (datomic/start-db! component)
    :redis (redis/start-db! component)))

(defn setup-schemas
  [component]
  (case (config/db-type component)
    :datomic (datomic/setup-schemas component)
    :redis (redis/setup-schemas component)))

(defn setup-subscribers
  [component]
  (case (config/db-type component)
    :datomic (datomic/setup-subscribers component)
    :redis (redis/setup-subscribers component)))

(defn run-setup-tasks
  [component]
  (log/debug "Starting setup tasks ...")
  (setup-schemas component)
  (setup-subscribers component)
  (log/debug "Finished setup tasks."))

(defn stop-db!
  [component]
  (case (config/db-type component)
    :datomic (datomic/stop-db! component)
    :redis (redis/stop-db! component)))

(defn add-connection
  [component]
  (case (config/db-type component)
    :datomic (datomic/add-connection component)
    :redis (redis/add-connection component)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Components   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord DataBase []
  component/Lifecycle

  (start [component]
    (log/info "Starting db component ...")
    (log/debug "Database component backend is" (config/db-type component))
    (start-db! component)
    (let [component (add-connection component)]
      (run-setup-tasks component)
      component))

  (stop [component]
    (log/info "Stopping db component ...")
    (let [a "b"]
      (log/debug "Stopped db component.")
      (stop-db! component)
      (db-core/remove-connection component))))

(defn create-db-component
  ""
  []
  (->DataBase))
