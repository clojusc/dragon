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
  (del-query [this schema-key schema-arg])
  (get-all-authors [this])
  (get-all-categories [this])
  (get-all-data [this post-key])
  (get-all-dates [this])
  (get-all-metadata [this])
  (get-all-stats [this])
  (get-all-tags [this])
  (get-all-uri-paths [this])
  (get-all-years [this])
  (get-author-posts [this author])
  (get-category-freqs [this])
  (get-category-max-count [this])
  (get-category-posts [this category])
  (get-category-stats [this])
  (get-category-totals [this])
  (get-first-n-keys [this n] [this offset n])
  (get-keys [this])
  (get-last-n-keys [this n] [this offset n])
  (get-n-keys [this n order] [this offset n order])
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
  (get-raw [this any-key])
  (get-tag-freqs [this])
  (get-tag-max-count [this])
  (get-tag-posts [this tag])
  (get-tag-stats [this])
  (get-tag-totals [this])
  (get-text-stats [this])
  (get-total-char-count [this])
  (get-total-line-count [this])
  (get-total-word-count [this])
  (get-year-posts [this year])
  (pipeline [this lines])
  (post-changed? [this post-key checksum])
  (set-add-query [this schema-key schema-arg post-keys])
  (set-all-checksums [this value])
  (set-all-data [this post-key post-data])
  (set-author-posts [this author post-keys])
  (set-category-posts [this category post-keys])
  (set-category-stats [this data])
  (set-keys [this post-keys])
  (set-members-query [this schema-key schema-arg])
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
  (set-rem-query [this schema-key schema-arg items])
  (set-tag-posts [this tag post-keys])
  (set-tag-stats [this data])
  (set-text-stats [this data])
  (set-year-posts [this year post-keys])
  (sorted-set-members-query [this schema-key schema-arg])
  (uri-path->file [this uri-path]))

(extend RedisQuerier
        DBQuerier
        redis/query-behaviour)

(defn new-querier
  [component conn]
  (case (config/db-type component)
    :redis (redis/new-querier component conn)))
