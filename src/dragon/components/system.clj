(ns dragon.components.system
  (:require [com.stuartsierra.component :as component]
            [dragon.components.config :as config]
            [dragon.components.event :as event]
            [dragon.components.logging :as logging]))

(defn initialize []
  (component/system-map
   :config (config/create-config-component)
   :logging (component/using
             (logging/create-logging-component)
             [:config])
   :event (component/using
           (event/create-event-component)
           [:config :logging])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Managment Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn stop
  ([system]
   (component/stop system))
  ([system component-key]
   (->> system
        (component-key)
        (component/stop)
        (assoc system component-key))))

(defn start
  ([]
   (start (initialize)))
  ([system]
   (component/start system))
  ([system component-key]
   (->> system
        (component-key)
        (component/start)
        (assoc system component-key))))

(defn restart
  ([system]
   (-> system
       (stop)
       (start)))
  ([system component-key]
   (-> system
       (stop component-key)
       (start component-key))))

