(ns dragon.data.sources.core
  (:require
    [clojure.java.shell :as shell]
    [dragon.components.config :as config]
    [dragon.data.sources.impl.common :as common]
    [dragon.data.sources.impl.redis :as redis]
    [dragon.util :as util]
    [taoensso.timbre :as log])
  (:import
    (dragon.data.sources.impl.redis RedisQuerier RedisConnector)))

(defprotocol DBConnector
  (execute-db-command! [this])
  (setup-schemas [this])
  (setup-subscribers [this])
  (add-connection [this])
  (remove-connection [this]))

(extend RedisConnector
        DBConnector
        (merge common/connection-behaviour
               redis/connection-behaviour))

(defn new-connector
  [component]
  (case (config/db-type component)
    :redis (redis/new-connector component)))

(defprotocol DBQuerier
  (get-post-checksum [this post-key])
  (get-post-content [this post-key])
  (get-post-keys [this] [this schema-glob])
  (get-post-metadata [this post-key])
  (get-post-tags [this post-key])
  (get-post-stats [this post-key])
  (get-all-categories [this])
  (get-all-data [this post-key])
  (get-all-tags [this])
  (get-all-stats [this])
  (get-raw [this any-key])
  (post-changed? [this post-key])
  (save-post [this data])
  (set-all-data [this post-key data])
  (set-content [this post-key data])
  (set-file-data [this post-key data])
  (set-metadata [this post-key data])
  (set-post-checksum [this post-key checksum])
  (set-posts-checksums [this checksum]))

(extend RedisQuerier
        DBQuerier
        redis/query-behaviour)

(defn new-querier
  [component]
  (case (config/db-type component)
    :redis (redis/new-querier component)))
