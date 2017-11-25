(ns dragon.data.sources.impl.redis.docker
  (:require
    [clojure.java.io :as io]
    [dragon.config.core :as config]
    [dragon.data.sources.impl.common :as common]
    [dragon.util :as util]
    [taoensso.timbre :as log]
    [trifl.fs :as fs]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dragon Connection Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord RedisDockerConnector [component])

(defn new-connector
  [component]
  (->RedisDockerConnector component))

(defn start-db!
  [this]
  (common/execute-db-command! this))

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
  (let [id-file (:container-id-file (config/db-config (:component this)))]
    (when (fs/exists? (io/as-file id-file))
      (->> (slurp id-file)
           (util/shell! "docker" "stop")
           vec)
      (util/shell! "rm" id-file))))

(def connection-behaviour
  {:start-db! start-db!
   :add-connection add-connection
   :stop-db! stop-db!})
