(ns dragon.components.system
  (:require [com.stuartsierra.component :as component]
            [dragon.components.config :as config]
            [dragon.components.db :as db]
            [dragon.components.event :as event]
            [dragon.components.httpd :as httpd]
            [dragon.components.logging :as logging]
            [dragon.config :refer [build] :rename {build build-config}]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Common Configuration Components   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cfg config/create-config-component)

(defn log
  []
  (component/using
   (logging/create-logging-component)
   [:config]))

(defn data
  []
  (component/using
   (db/create-db-component)
   [:config :logging]))

(defn evt
  []
  (component/using
   (event/create-event-component)
   [:config :logging :db]))

(defn http
  []
  (component/using
   (httpd/create-httpd-component)
   [:config :logging :event]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Intilizations   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn initialize-default
  ([]
    (initialize-default build-config))
  ([config-builder]
    (component/system-map
     :config (cfg config-builder)
     :logging (log)
     :db (data)
     :event (evt))))

(defn initialize-bare-bones
  ([]
    (initialize-bare-bones build-config))
  ([config-builder]
    (component/system-map
     :config (cfg config-builder)
     :event (evt))))

(defn initialize-with-web
  ([]
    (initialize-with-web build-config))
  ([config-builder]
    (component/system-map
     :config (cfg config-builder)
     :logging (log)
     :db (data)
     :event (evt)
     :httpd (http))))

(def init
  {:default initialize-default
   :basic initialize-bare-bones
   :web initialize-with-web})

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

