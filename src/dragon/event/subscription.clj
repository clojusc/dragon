(ns dragon.event.subscription
  (:require [dragon.event.names :as names]
            [dragon.event.system.core :as event]
            [taoensso.timbre :as log]))

(def subscribers
  {names/process-all-pre []
   names/process-all-post []
   names/run-cli [:a]
   names/read-source-pre []
   names/read-source-post []
   names/parse-content-pre []
   names/parse-content-post []
   names/write-output-pre []
   names/write-output-post []
   names/generate-routes-pre []
   names/generate-routes-post []})

(defn subscribe-all-event
  ""
  [system [event-type subscribers]]
  (doseq [func subscribers]
    (log/infof "Subscribing to %s ..." event-type)
    ;;(event/subscribe system event-type func)
    (event/subscribe system event-type)))

(defn subscribe-all
  ""
  [system]
  (doall
   (map (partial subscribe-all-event system) subscribers)))
