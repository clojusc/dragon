(ns dragon.components.system
  (:require [com.stuartsierra.component :as component]
            [dragon.components.config :as config]
            [dragon.components.db :as db]
            [dragon.components.event :as event]
            [dragon.components.httpd :as httpd]
            [dragon.components.logging :as logging]
            [dragon.config.core :refer [build]
                                :rename {build build-config}]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Common Configuration Components   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn cfg
  [builder]
  {:config (config/create-config-component builder)})

(def log
  {:logging (component/using
             (logging/create-logging-component)
             [:config])})

(def evt-no-log
  {:event (component/using
           (event/create-event-component)
           [:config])})

(def data-no-log
  {:db (component/using
        (db/create-db-component)
        [:config :event])})

(def evt
  {:event (component/using
           (event/create-event-component)
           [:config :logging])})

(def data
  {:db (component/using
        (db/create-db-component)
        [:config :logging :event])})

(def http
  {:httpd (component/using
           (httpd/create-httpd-component)
           [:config :logging :db :event])})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Intilizations   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn initialize-default
  ([]
    (initialize-default build-config))
  ([config-builder]
    (component/map->SystemMap
      (merge (cfg config-builder)
             log
             data
             evt))))

(defn initialize-bare-bones
  ([]
    (initialize-bare-bones build-config))
  ([config-builder]
    (component/map->SystemMap
      (merge (cfg config-builder)
             data-no-log
             evt-no-log))))


(defn initialize-with-web
  ([]
    (initialize-with-web build-config))
  ([config-builder]
    (component/map->SystemMap
      (merge (cfg config-builder)
             log
             data
             evt
             http))))

(def init-lookup
  {:default #'initialize-default
   :basic #'initialize-bare-bones
   :web #'initialize-with-web})

(defn init
  ([]
    (init :default))
  ([mode]
    (init mode build-config))
  ([mode config-builder]
    ((mode init-lookup config-builder))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Managment Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def stop #'component/stop)

(defn start
  ([config-builder]
   (start (initialize-default config-builder)))
  ([config-builder system-type]
    (component/start (init system-type config-builder))))

(defn restart
  ([system]
   (-> system
       (component/stop)
       (component/start))))
