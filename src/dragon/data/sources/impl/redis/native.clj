(ns dragon.data.sources.impl.redis.native
  (:require
    [clojure.java.io :as io]
    [clojure.java.shell :as shell]
    [dragon.components.config :as config]
    [dragon.data.sources.impl.common :as common]
    [dragon.util :as util]
    [taoensso.timbre :as log]
    [trifl.fs :as fs]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dragon Connection Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord RedisNativeConnector [component])

(defn new-connector
  [component]
  (->RedisNativeConnector component))

(defn start-db!
  [this]
  (let [start-cfg (config/db-start-config (:component this))
        data-dir (:data-dir start-cfg)
        args (:args start-cfg)]
    (shell/with-sh-dir data-dir
      (log/debugf "Running command in %s ..." data-dir)
      (log/debug "Using shell/sh args:" (vec args))
      (apply util/spawn! args))))

(defn setup-schemas
  [this]
  )

(defn setup-subscribers
  [this]
  )

(defn add-connection
  [this]
  (assoc (:component this) :conn (config/db-conn (:component this))))

(defn stop-db!
  [this]
  (util/shell! "redis-cli" "shutdown"))

(def connection-behaviour
  {:start-db! start-db!
   :add-connection add-connection
   :stop-db! stop-db!})
