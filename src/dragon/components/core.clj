(ns dragon.components.core
  (:require
    [com.stuartsierra.component :as component]
    [clojusc.config.unified.components.config :as config]
    [dragon.components.db :as db]
    [dragon.components.event :as event]
    [dragon.components.httpd :as httpd]
    [dragon.components.logging :as logging]
    [dragon.components.responder :as responder]
    [dragon.components.watcher :as watcher]
    [dragon.config.core :as config-lib]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Common Configuration Components   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn cfg
  [cfg-data]
  {:config (config/create-component cfg-data)})

(def log
  {:logging (component/using
             (logging/create-component)
             [:config])})

(def evt-no-log
  {:event (component/using
           (event/create-component)
           [:config])})

(def data-no-log
  {:db (component/using
        (db/create-component)
        [:config :event])})

(def evt
  {:event (component/using
           (event/create-component)
           [:config :logging])})

(def data
  {:db (component/using
        (db/create-component)
        [:config :logging :event])})

(def http
  {:httpd (component/using
           (httpd/create-component)
           [:config :logging :db :event])})

(def wtchr
  {:watcher (component/using
             (watcher/create-component)
             [:config :event])})

(defn rspndr
  ([]
    (rspndr {}))
  ([handlers]
    {:responder (component/using
                 (responder/create-component handlers)
                 [:config :event :httpd :watcher])}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Initializations   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn initialize-default
  [cfg-data]
  (component/map->SystemMap
    (merge (cfg cfg-data)
           log
           data
           evt)))

(defn initialize-bare-bones
  [cfg-data]
  (component/map->SystemMap
    (merge (cfg cfg-data)
           data-no-log
           evt-no-log)))

(defn initialize-with-web
  [cfg-data]
  (component/map->SystemMap
    (merge (cfg cfg-data)
           log
           data
           evt
           http
           wtchr
           (rspndr))))

(def init-lookup
  {:default #'initialize-default
   :basic #'initialize-bare-bones
   :web #'initialize-with-web})

(defn init
  ([]
    (init :default (config-lib/data)))
  ([mode cfg-data]
    ((mode init-lookup) cfg-data)))
