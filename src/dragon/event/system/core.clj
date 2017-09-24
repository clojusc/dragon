(ns dragon.event.system.core
  (:require [clojure.core.async :as async]
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
  (let [pubsub (get-in system [:event :pubsub])
        topic (get-topic pubsub)
        msg {topic event-type
             :data data}]
    (log/info "Publishing message ...")
    (log/infof "Routing with topic %s and tag %s ..."
               topic event-type)
    ;; XXX Add messaging API function (get-payload msg)
    ;; XXX Add messaging API function (get-route msg) that dissoc's data
    (log/info "Sending message data:" (:data msg))
    (async/>!! (get-chan pubsub) msg))
  data)

(defn subscribe
  ""
  ([system event-type]
   (subscribe system event-type (fn [s m]
                                  (log/infof
                                   "Got system: %s\nGot msg: %s" s m))))
  ([system event-type func]
   (let [pubsub (get-in system [:event :pubsub])]
     (async/go-loop []
       (when-let [msg (async/<! (get-sub pubsub event-type))]
         (log/info "Received subscribed message.")
         ;; XXX use the message API functions in the next two lines
         (log/info "Routing info:" (dissoc msg :data))
         (log/info "Message data:" (:data msg))
         (log/info "Callback function:" func)
         (func system msg)
         (recur))))))

