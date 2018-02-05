(ns dragon.components.core
  "System component access functions."
  (:require
    [dragon.util :as util]))

(def dataflow-keys [:pubsub :dataflow])

(defn get-pubsub
  ""
  [system]
  (get-in system [:event :pubsub]))

(defn get-dataflow-pubsub
  ""
  [system]
  (get-in system (concat [:event] dataflow-keys)))

(defn get-db-conn
  [system]
  (get-in system [:db :conn]))

(defn get-db-connector
  [system]
  (get-in system [:db :connector]))

(defn get-db-querier
  [system]
  (get-in system [:db :querier]))
