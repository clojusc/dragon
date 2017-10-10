(ns dragon.data.sources.datomic
  (:require [clojure.core.async :as async]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [datomic.client :as datomic]
            [dragon.config :as config]
            [dragon.data.sources.core :as db-core]
            [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Schemas & Support Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def subscriber-registry-topic
  {:db/ident :registry/topic
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one
   :db/doc "The topic with which the pubsub was created."})

(def subscriber-registry-tag
  {:db/ident :registry/tag
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one
   :db/doc "The tag given to a message."})

(def subscriber-registry-subscribers
  {:db/ident :registry/subscribers
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/many
   :db/doc "The subscribers for a message tag."})

(def subscriber-registry-schema [
  subscriber-registry-topic
  subscriber-registry-tag
  subscriber-registry-subscribers])

(def default-schemas
  [subscriber-registry-schema])

(defn add-schema
  [conn schema]
  (log/trace "\tAdding schema:" schema)
  (->> schema
       (hash-map :tx-data)
       (datomic/transact conn)
       (async/<!!)))

(defn add-schemas
  ([conn]
    (add-schemas conn default-schemas))
  ([conn schemas]
    (log/debug "Adding schemas ...")
    (map (partial add-schema conn) schemas)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dragon DB API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn start-db!
  [component]
  (async/go-loop []
    (log/debug "Starting database ...")
    (let [ch (async/thread (db-core/execute-db-command! component))]
      (when-let [result (async/<! ch)]
        (log/info "Previous database processes had been shutdown.")
        result)))
  (Thread/sleep (config/db-start-delay component)))

(defn setup-schemas
  [component]
  ;; XXX Add check to see if schema already exists ... maybe? Or compact the db
  ;;     after it's been run many times?
  (log/trace (-> component
                 :conn
                 (add-schemas)
                 vec)))

(defn setup-subscribers
  [component]
  )

(defn add-connection
  [component]
  (let [conn-cfg (config/db-conn component)
        conn (async/<!! (datomic/connect conn-cfg))]
    (log/debug "Using configuration:" conn-cfg)
    (if (:cognitect.anomalies/category conn)
      (do
        (log/error (:cognitect.anomalies/message conn))
        component)
      (do
        (log/debug "Started db component.")
        (assoc component :conn conn)))))

(defn stop-db!
  [component]
  (->> (shell/sh "ps" "-eo" "pid,command")
       :out
       (shell/sh "grep" "datomic.peer-server" :in)
       :out
       (#(string/split % #"\n"))
       (map (comp first #(string/split % #"\s")))
       vec
       (concat ["xargs" "kill"])
       (apply shell/sh)))
