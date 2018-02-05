(ns dragon.data.sources.impl.datomic
  (:require
    [clojure.core.async :as async]
    [clojure.java.shell :as shell]
    [clojure.string :as string]
    [datomic.client :as datomic]
    [dragon.components.config :as config]
    [dragon.data.sources.impl.common :as common]
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

(defrecord DatomicQuerier [component])

(defn new-querier
  [component]
  (->DatomicQuerier component))

(def query-behaviour
  {})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dragon DB API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord DatomicConnector [component])

(defn new-connector
  [component]
  (->DatomicConnector component))

(defn start-db!
  [this]
  (async/go-loop []
    (log/debug "Starting database ...")
    (let [ch (async/thread (common/execute-db-command! this))]
      (when-let [result (async/<! ch)]
        (log/info "Previous database processes had been shutdown.")
        result)))
  (Thread/sleep (config/db-start-delay (:component this))))

(defn setup-schemas
  [this]
  ;; XXX Add check to see if schema already exists ... maybe? Or compact the db
  ;;     after it's been run many times?
  (log/trace (-> this
                 :component
                 :conn
                 (add-schemas)
                 vec)))

(defn setup-subscribers
  [this]
  )

(defn add-connection
  [this]
  (let [conn-cfg (config/db-conn (:component this))
        conn (async/<!! (datomic/connect conn-cfg))]
    (log/debug "Using configuration:" conn-cfg)
    (if (:cognitect.anomalies/category conn)
      (do
        (log/error (:cognitect.anomalies/message conn))
        this)
      (do
        (log/debug "Started db component.")
        (assoc (:component this) :conn conn)))))

(defn stop-db!
  [this]
  (->> (shell/sh "ps" "-eo" "pid,command")
       :out
       (shell/sh "grep" "datomic.peer-server" :in)
       :out
       (#(string/split % #"\n"))
       (map (comp first #(string/split % #"\s")))
       vec
       (concat ["xargs" "kill"])
       (apply shell/sh)))

(def connection-behaviour
  {:start-db! start-db!
   :setup-schemas setup-schemas
   :add-connection add-connection
   :stop-db! stop-db!})
