(ns dragon.components.core
  (:require
    [com.stuartsierra.component :as component]
    [clojusc.config.unified.components.config :as config]
    [clojusc.process.manager.components.docker :as docker]
    [dragon.components.db :as db]
    [dragon.components.event :as event]
    [dragon.components.httpd :as httpd]
    [dragon.components.logging :as logging]
    [dragon.components.responder :as responder]
    [dragon.components.watcher :as watcher]
    [dragon.config :as config-lib]))

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

(def create-evt (partial component/using (event/create-component)))

(def create-redis
  (partial component/using
           (docker/create-component
             :redis
            {:image-id "redis:5.0.3-alpine"
             :ports ["127.0.0.1:6379:6379"
                     "127.0.0.1:6380:6380"]
             :volumes [(str (System/getProperty "user.dir") "/data/redis:/data")]
             :container-id-file "/tmp/dragon-redis-container-id"})))

(def create-db (partial component/using (db/create-component)))

(def evt {:event (create-evt [:config :logging])})
(def evt-no-log {:event (create-evt [:config])})
(def redis {:redis-server (create-redis [:config :logging])})
(def redis-no-log {:redis-server (create-redis [:config])})
(def data {:db (create-db [:config :logging :event :redis-server])})
(def data-no-log {:db (create-db [:config :event :redis-server])})

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
           redis
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
           redis
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
