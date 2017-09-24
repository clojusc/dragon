(ns dragon.event.subscription
  (:require [com.stuartsierra.component :as component]
            [dragon.event.message :as message]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.util :as util]
            [taoensso.timbre :as log]))

(defn debug-subscriber
  [system msg]
  (log/debug "Debug subscriber got system with keys:" (keys system))
  (log/debug "Debug subscriber got msg payload:" (message/get-payload msg)))

(defn trace-subscriber
  [system msg]
  (log/trace "Trace subscriber got system:" system)
  (log/trace "Trace subscriber got full msg:" msg))

(defn shutdown-subscriber
  [system msg]
  (log/info "Shutting down system ...")
  (component/stop system)
  (shutdown-agents))

(def subscribers
  {tag/subscribers-added [:default]
   tag/process-all-pre [:default]
   tag/process-all-post [:default]
   tag/run-cli [:default]
   tag/read-source-pre [:default]
   tag/read-source-post [:default]
   tag/parse-file-pre [:default]
   tag/parse-file-post [:default]
   tag/parse-content-pre [:default]
   tag/parse-content-post [:default]
   tag/write-output-pre [:default]
   tag/write-output-post [:default]
   tag/generate-routes-pre [:default]
   tag/generate-routes-post [:default]
   tag/shutdown-cli [shutdown-subscriber]})

(defn subscribe-all-event
  ""
  [system event-type subscriber-funcs]
    (doseq [func subscriber-funcs]
      (log/debugf "Subscribing to %s ..." event-type)
      (event/subscribe system event-type debug-subscriber)
      (event/subscribe system event-type trace-subscriber)
      (if (= func :default)
        (event/subscribe system event-type)
        (event/subscribe system event-type func))))

(defn subscribe-all
  ""
  [system-or-component]
  (let [system (util/component->system system-or-component)]
    (doseq [[event-type subscriber-funcs] subscribers]
      (subscribe-all-event system
                           event-type
                           subscriber-funcs))))
