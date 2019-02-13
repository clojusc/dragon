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
  (setup-schema [this])
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
  (cmd [this args])
  (get-keys [this])
  (get-n-keys [this n order] [this offset n order])
  (get-first-n-keys [this n] [this offset n])
  (get-last-n-keys [this n] [this offset n])
  (get-post-category [this post-key])
  (get-post-checksum [this post-key])
  (get-post-content [this post-key])
  (get-post-content-source [this post-key])
  (get-post-dates [this post-key])
  (get-post-excerpts [this post-key])
  (get-post-metadata [this post-key])
  (get-post-stats [this post-key])
  (get-post-tags [this post-key])
  (get-post-uri-path [this post-key])
  (get-all-authors [this])
  (get-all-categories [this])
  (get-all-data [this post-key])
  (get-all-metadata [this])
  (get-all-tags [this])
  (get-all-stats [this])
  (get-category-freqs [this])
  (get-tag-freqs [this])
  (get-total-char-count [this])
  (get-total-line-count [this])
  (get-total-word-count [this])
  (get-raw [this any-key])
  (post-changed? [this post-key checksum])
  (set-keys [this post-keys])
  (set-post-category [this post-key value])
  (set-post-checksum [this post-key value])
  (set-post-content [this post-key value])
  (set-post-content-source [this post-key value])
  (set-post-dates [this post-key value])
  (set-post-excerpts [this post-key value])
  (set-post-metadata [this post-key value])
  (set-post-stats [this post-key value])
  (set-post-tags [this post-key value])
  (set-post-uri-path [this post-key value])
  (set-all-checksums [this value])
  (set-all-data [this post-key post-data]))

(extend RedisQuerier
        DBQuerier
        redis/query-behaviour)

(defn new-querier
  [component conn]
  (case (config/db-type component)
    :redis (redis/new-querier component conn)))
