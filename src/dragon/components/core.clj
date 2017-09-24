(ns dragon.components.core
  (:require [com.stuartsierra.component :as component]
            [dragon.components.config :as config]
            [dragon.components.event :as event]
            [dragon.components.logging :as logging]))

(defn init-system []
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
   (start (init-system)))
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Access Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-config
  ""
  [system]
  (get-in system [:config :dragon]))

(defn get-pubsub
  ""
  [system]
  (get-in system [:event :pubsub]))
