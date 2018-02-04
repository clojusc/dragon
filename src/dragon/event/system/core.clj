(ns dragon.event.system.core
  (:require
    [clojure.core.async :as async]
    [dragon.components.core :as components]
    [dragon.event.message :as message]
    [dragon.event.system.impl :as impl]
    [dragon.event.topic :as topic]
    [dragon.util :as util]
    [potemkin :refer [import-vars]]
    [taoensso.timbre :as log])
  (:import
    [dragon.event.system.impl PubSub]))

(import-vars
 [impl create-pubsub
       create-dataflow-pubsub])

(defprotocol PubSubAPI
  "The API for Dragon pubsub messenging."
  (get-topic [this]
    "Get the topic with which the messenger was instantiated.")
  (get-chan [this]
    "Get the core.async channel associated with the publisher.")
  (get-pub [this]
    "Get the core.async pub associated with the publisher.")
  (get-sub [this tag]
    "Create and return a subscriber channel for a given tag (event-type).")
  (delete [this]
    "Delete the publisher."))

(extend PubSub
        PubSubAPI
        impl/pubsub-behaviour)

(defn publish
  ""
  ([system-or-component event-type]
   (publish system-or-component event-type {}))
  ([system-or-component event-type data]
   (when-not (nil? system-or-component)
     (let [system (util/component->system system-or-component)
           dataflow (components/get-dataflow-pubsub system)
           topic (get-topic dataflow)
           msg (message/new-dataflow-event event-type data)]
       (log/debug "Publishing message to" (message/get-route msg))
       (log/trace "Sending message data:" (message/get-payload msg))
       (async/>!! (get-chan dataflow) msg))
     data)))

(defn publish->
  ""
  ([other-data system-or-component event-type]
   (publish-> other-data system-or-component event-type {}))
  ([other-data system-or-component event-type data]
   (publish system-or-component event-type data)
   other-data))

(defn publish->>
  ""
  ([system-or-component event-type other-data]
   (publish->> system-or-component event-type {} other-data))
  ([system-or-component event-type data other-data]
   (publish system-or-component event-type data)
   other-data))

(defn subscribe
  ""
  ([system event-type]
   (subscribe system event-type (fn [s m]
                                  (log/warn
                                   "Using default subscriber callback for route"
                                   (message/get-route m)))))
  ([system event-type func]
   (when-not (nil? system)
     (let [dataflow (components/get-dataflow-pubsub system)]
       (async/go-loop []
         (when-let [msg (async/<! (get-sub dataflow event-type))]
           (log/debug "Received subscribed message for" (message/get-route msg))
           (log/trace "Message data:" (message/get-payload msg))
           (log/trace "Callback function:" func)
           (func system msg)
           (recur)))))))

