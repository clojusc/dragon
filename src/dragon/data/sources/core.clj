(ns dragon.data.sources.core
  (:require [clojure.java.shell :as shell]
            [dragon.config.core :as config]
            [dragon.data.sources.impl.common :as common]
            [dragon.data.sources.impl.datomic :as datomic]
            [dragon.data.sources.impl.redis :as redis]
            [dragon.data.sources.impl.redis.docker :as redis-docker]
            [dragon.util :as util]
            [taoensso.timbre :as log])
  (:import (dragon.data.sources.impl.datomic DatomicConnector
                                             DatomicQuerier)
           (dragon.data.sources.impl.redis RedisQuerier)
           (dragon.data.sources.impl.redis.docker RedisDockerConnector)))

(defprotocol DBConnector
  (start-db! [this])
  (execute-db-command! [this])
  (setup-schemas [this])
  (setup-subscribers [this])
  (add-connection [this])
  (remove-connection [this])
  (stop-db! [this]))

(extend DatomicConnector
        DBConnector
        (merge common/connection-behaviour
               datomic/connection-behaviour))

(extend RedisDockerConnector
        DBConnector
        (merge common/connection-behaviour
               redis-docker/connection-behaviour))

(defn new-connector
  [component]
  (case (config/db-type component)
    :redis-docker (redis-docker/new-connector component)
    :datomic (datomic/new-connector component)))

(defprotocol DBQuerier
  (get-post-checksum [this post-key])
  (get-post-content [this post-key])
  (get-post-metadata [this post-key])
  (get-post-tags [this post-key])
  (get-post-stats [this post-key])
  (get-all-categories [this])
  (get-all-data [this post-key])
  (get-all-tags [this])
  (get-all-stats [this])
  (post-changed? [this post-key])
  (save-post [this data])
  (set-all-data [this post-key data])
  (set-content [this post-key data])
  (set-file-data [this post-key data])
  (set-metadata [this post-key data])
  (set-post-checksum [this post-key checksum])
  (set-posts-checksums [this checksum]))

(extend DatomicQuerier
        DBQuerier
        datomic/query-behaviour)

(extend RedisQuerier
        DBQuerier
        redis/query-behaviour)

(defn new-querier
  [component]
  (case (config/db-type component)
    :redis-docker (redis/new-querier component)
    :datomic (datomic/new-querier component)))
