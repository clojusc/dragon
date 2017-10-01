(ns dragon.components.system
  (:require [com.stuartsierra.component :as component]
            [dragon.components.config :as config]
            [dragon.components.event :as event]
            [dragon.components.httpd :as httpd]
            [dragon.components.logging :as logging]))

(defn initialize-default [config-builder]
  (component/system-map
   :config (config/create-config-component config-builder)
   :logging (component/using
             (logging/create-logging-component)
             [:config])
   :event (component/using
           (event/create-event-component)
           [:config :logging])))

(defn initialize-bare-bones [config-builder]
  (component/system-map
   :config (config/create-config-component config-builder)
   :event (component/using
           (event/create-event-component)
           [:config])))

(defn initialize-with-web [config-builder]
  (component/system-map
   :config (config/create-config-component config-builder)
   :logging (component/using
             (logging/create-logging-component)
             [:config])
   :event (component/using
           (event/create-event-component)
           [:config :logging])
   :httpd (component/using
           (httpd/create-httpd-component)
           [:config :logging :event])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Managment Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def stop #'component/stop)

(defn start
  ([config-builder]
   (start (initialize-default config-builder)))
  ([config-builder system-type]
   (case system-type
     :web (component/start (initialize-with-web config-builder))
     :basic (component/start (initialize-bare-bones config-builder))
     :cli (component/start (initialize-default config-builder)))))

(defn restart
  ([system]
   (-> system
       (component/stop)
       (component/start))))

