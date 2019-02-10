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
;;;   DB Component API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn db-conn
  [system]
  (get-in system [:db :conn]))

(defn db-connector
  [system]
  (get-in system [:db :connector]))

(defn db-querier
  [system]
  (get-in system [:db :querier]))

(defn cmd
  [system & args]
  (data-source/cmd (db-querier system) args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Support Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-setup-tasks
  [connector]
  (log/debug "Starting db setup tasks ...")
  (data-source/setup-schema connector)
  (data-source/setup-subscribers connector)
  (log/debug "Finished db setup tasks."))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Lifecycle Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord DataBase [conn connector querier])

(defn start
  [this]
  (log/info "Starting db component ...")
  (log/trace "component keys:" (keys this))
  (log/debug "Database component backend is" (config/db-type this))
  (let [connector (data-source/new-connector this)
        conn (config/db-conn this)
        querier (data-source/new-querier this conn)]
    (run-setup-tasks connector)
    (assoc this :connector connector
                :querier querier
                :conn conn)))

(defn stop
  [this]
  (log/info "Stopping db component ...")
  (log/trace "component keys:" (keys this))
  (log/trace "config:" (:config this))
  (let [raw-connector (:connector this)
        connector (if (util/atom? raw-connector)
                    @raw-connector
                    raw-connector)]
    (log/info "Stopped db component.")
    (assoc this :connector nil
                :querier nil)))

(def lifecycle-behaviour
  {:start start
   :stop stop})

(extend DataBase
  component/Lifecycle
  lifecycle-behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-component
  ""
  []
  (map->DataBase {}))
