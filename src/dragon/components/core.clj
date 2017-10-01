(ns dragon.components.core
  "System component access functions.")

(def dataflow-keys [:pubsub :dataflow])

(defn get-config
  ""
  [system & args]
  (let [base-keys [:config :dragon]]
    (if-not (seq args)
      (get-in system base-keys)
      (get-in system (concat base-keys args)))))

(defn get-pubsub
  ""
  [system]
  (get-in system [:event :pubsub]))

(defn get-dataflow-pubsub
  ""
  [system]
  (get-in system (concat [:event] dataflow-keys)))
