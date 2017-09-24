(ns dragon.event.subscription
  (:require [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [taoensso.timbre :as log]))

(def subscribers
  {tag/process-all-pre []
   tag/process-all-post []
   tag/run-cli [(constantly true)]
   tag/read-source-pre []
   tag/read-source-post []
   tag/parse-content-pre []
   tag/parse-content-post []
   tag/write-output-pre []
   tag/write-output-post []
   tag/generate-routes-pre []
   tag/generate-routes-post []})

(defn component->system
  ""
  [system-or-component]
  (if (contains? system-or-component :event)
    system-or-component
    {:event system-or-component}))

(defn subscribe-all-event
  ""
  [system-or-component [event-type subscribers]]
  (let [system (component->system system-or-component)]
    (doseq [func subscribers]
      (log/infof "Subscribing to %s ..." event-type)
      ;;(event/subscribe system event-type func)
      (event/subscribe system event-type))))

(defn subscribe-all
  ""
  [system]
  (doall
   (map (partial subscribe-all-event system) subscribers)))
