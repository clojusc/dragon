(ns dragon.components.responder
  (:require
    [clojure.core.async :as async]
    [clojusc.twig :as logger]
    [dragon.event.tag :as tag]
    [com.stuartsierra.component :as component]
    [dragon.components.config :as config]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Constants & Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def initial-data {
  :content {
    :request-count 0
    :timeout 15}
  :css {
    :request-count 0
    :timeout 10}
  :restart-system {
    :request-count 0
    :timeout 20}})

(def default-handlers {
  tag/content-regen
  tag/css-regen (constantly true)
  tag/system-restart (constantly true)}

(defn get-channel-pair
  [key]
  [key (async/timeout (get-in initial-data key :timeout))])

(defn make-channels
  []
  (->> (keys initial-data)
       (map get-channel-pair)
       (into {})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Responder Component API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-responder
  [system]
  (:responder system))

(defn get-responder-data
  [system]
  (or (get-in system [:responder :data])
      (:data system)))

(defn get-channels
  [system]
  (or (get-in system [:responder :channels])
      (:channels system)))

(defn initialize-data
  []
  (atom initial-data))

(defn inc-request-count
  [system key]
  (swap! (get-responder-data system)
         (fn [d k] (update-in d [k :request-count] inc)) key))

(defn get-request-count
  [system key]
  (get-in (get-responder-data system) [key :request-count]))

(defn close-channels
  [system]
  (for [ch (get-channels system)]
    (async/close! ch)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Lifecycle Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord Responder [data channels])

(defn start
  [this]
  (log/info "Starting responder component ...")
  (log/debug "Started responder component.")
  this)

(defn stop
  [this]
  (log/info "Stopping responder component ...")
  (close-channels this)
  (log/debug "Stopped responder component.")
  this)

(def lifecycle-behaviour
  {:start start
   :stop stop})

(extend Responder
  component/Lifecycle
  lifecycle-behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-component
  "Handlers should be a map whose keys are dragon event namespace keys and
  whose values are functions to be called to handle the events represented by
  the keys."
  [handlers]
  (map->Responder {
    :data (initialize-data)
    :channels (make-channels)
    :handlers (merge default-handlers handlers)}))

(defn doit
  []
  (let [limit 10]
    (log/info "Start!")
    (doseq [n (range limit)]
      (let [wait-seconds (+ (rand-int limit))]
        (async/go
          (async/<! (async/timeout (* wait-seconds 1000)))
          (log/infof "Waiting %s seconds ..." wait-seconds))
        (log/info "Done.")))))
