(ns dragon.components.db
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [datomic.client :as datomic]
            [dragon.components.core :as component-api]
            [dragon.config :as config]
            [dragon.event.subscription :as subscription]
            [dragon.event.tag :as tag]
            [dragon.event.topic :as topic]
            [taoensso.timbre :as log]))

(defn execute-db-command!
  [component]
  (let [start-cfg (config/db-start-config component)
        home (:home start-cfg)
        args (:args start-cfg)]
    (shell/with-sh-dir home
      (log/debugf "Running command in %s ..." home)
      (log/debug "Using shell/sh args:" args)
      (apply shell/sh args))))

(defn start-db!
  [component]
  (async/go-loop []
    (log/debug "Starting database ...")
    (let [ch (async/thread (execute-db-command! component))]
      (when-let [result (async/<! ch)]
        (log/info "Previous database processes had been shutdown.")
        result)))
  (Thread/sleep (config/db-start-delay component)))

(defn stop-db!
  [component]
  ;; XXX If we ever support more than one type of database, we can just do a
  ;;     case construct here ...
  (->> (shell/sh "ps" "-eo" "pid,command")
       :out
       (shell/sh "grep" "datomic.peer-server" :in)
       :out
       (#(string/split % #"\n"))
       (map (comp first #(string/split % #"\s")))
       vec
       (concat ["xargs" "kill"])
       (apply shell/sh)))

(defrecord DataBase []
  component/Lifecycle

  (start [component]
    (log/info "Starting db component ...")
    (start-db! component)
    (let [cfg (config/db-config component)
          conn (async/<!! (datomic/connect cfg))]
      (log/debug "Using configuration:" cfg)
      (if (:cognitect.anomalies/category conn)
        (do
          (log/error (:cognitect.anomalies/message conn))
          component)
        (do
          (log/debug "Started db component.")
          (assoc component :conn conn)))))

  (stop [component]
    (log/info "Stopping db component ...")
    (let [a "b"]
      (log/debug "Stopped db component.")
      (stop-db! component)
      (assoc component :conn nil))))

(defn create-db-component
  ""
  []
  (->DataBase))
