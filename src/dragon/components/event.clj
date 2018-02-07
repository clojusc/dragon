(ns dragon.components.event
  (:require
    [com.stuartsierra.component :as component]
    [dragon.event.subscription :as subscription]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [dragon.event.topic :as topic]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Event Component API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Lifecycle Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord Event [pubsub])

(defn start
  [this]
  (log/info "Starting event component ...")
  (log/debug "Started event component.")
  (let [dataflow (event/create-dataflow-pubsub)
        component (assoc-in this event/dataflow-keys dataflow)]
    (log/info "Adding subscribers ...")
    (subscription/subscribe-all component)
    component))

(defn stop
  [this]
  (log/info "Stopping event component ...")
  (if-let [pubsub-dataflow (get-in this event/dataflow-keys)]
    (event/delete pubsub-dataflow))
  (let [component (assoc-in this event/dataflow-keys nil)]
    (log/debug "Stopped event component.")
    component))

(def lifecycle-behaviour
  {:start start
   :stop stop})

(extend Event
  component/Lifecycle
  lifecycle-behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-component
  ""
  []
  (map->Event
   {:pubsub {}}))
