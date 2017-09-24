(ns dragon.event.system.core
  (:require [clojure.core.async :as async]
            [dragon.components.core :as components]
            [dragon.event.message :as message]
            [dragon.event.system.impl :as impl]
            [dragon.event.topic :as topic]
            [potemkin :refer [import-vars]]
            [taoensso.timbre :as log])
  (:import [dragon.event.system.impl PubSub]))

(import-vars
 [impl create-pubsub])

(defprotocol PubSubAPI
  "The API for Dragon pubsub messenging."
  (get-topic [this]
    "Get the topic with which the messenger was instantiated.")
  (get-chan [this]
    "Get the core.async channel associated with the publisher.")
  (get-pub [this]
    "Get the core.async pub associated with the publisher.")
  (get-sub [this] [this topic]
    "Create and return a subscriber channel for a given topic. If no topic is
    given, use the default.")
  (delete [this]
    "Delete the publisher."))

(extend PubSub
        PubSubAPI
        impl/pubsub-behaviour)

(defn publish
  ""
  [system event-type data]
  (let [pubsub (components/get-pubsub system)
        topic (get-topic pubsub)
        msg (message/new-dataflow-event event-type data)]
    (log/info "Publishing message ...")
    (log/info "Routing info:" (message/get-route msg))
    (log/info "Sending message data:" (message/get-payload msg))
    (async/>!! (get-chan pubsub) msg))
  data)

(defn subscribe
  ""
  ([system event-type]
   (subscribe system event-type (fn [s m]
                                  (log/infof
                                   "Got system: %s\nGot msg: %s" s m))))
  ([system event-type func]
   (let [pubsub (components/get-pubsub system)]
     (async/go-loop []
       (when-let [msg (async/<! (get-sub pubsub event-type))]
         (log/info "Received subscribed message.")
         (log/info "Routing info:" (message/get-route msg))
         (log/info "Message data:" (message/get-payload msg))
         (log/info "Callback function:" func)
         (func system msg)
         (recur))))))

