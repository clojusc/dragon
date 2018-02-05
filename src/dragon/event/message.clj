(ns dragon.event.message
  (:require
  	[dragon.event.topic :as topic]))

(defn new-dataflow-event
  [tag data]
  {topic/dataflow-events tag
   :data data})

(defn get-payload
  [msg]
  (:data msg))

(defn get-route
  [msg]
  (dissoc msg :data))
