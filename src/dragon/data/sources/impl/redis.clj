(ns dragon.data.sources.impl.redis
  (:require
    [clojure.java.io :as io]
    [clojure.set :as set]
    [clojure.string :as string]
    [dragon.components.config :as config]
    [dragon.data.sources.impl.common :as common]
    [dragon.util :as util]
    [taoensso.carmine :as car :refer [wcar]]
    [taoensso.timbre :as log]
    [trifl.fs :as fs])
  (:import
    (clojure.lang Keyword)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Constants & Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX This should be moved into a general redis support ns, not an
;;     implementation ... the schemas need to be references from other
;;     places in the code, and these should be wrapped with a protocol
;;     some place sensible
(defn schema
  "For the special case when `path-segment` is the string value `all-posts`,
  the cumulative data for all posts is referenced. This is applicable to
  `categoies`, `tags`, and `stats`."
  ([]
    {:keys "path-segments"})
  ([path-segment]
    {:category (str path-segment ":category")
     :checksum (str path-segment ":checksum")
     :content (str path-segment ":content")
     :content-source (str path-segment ":content-source")
     :dates (str path-segment ":dates")
     :excerpts (str path-segment ":excerpts")
     :metadata (str path-segment ":metadata")
     :stats (str path-segment ":stats")
     :tags (str path-segment ":tags")
     :uri-path (str path-segment ":uri-path")}))

(defn key->path-segment
  [schema-key]
  (first (string/split schema-key #":")))

(defn get-query
  [schema-key src-file]
  [:get (schema-key (schema src-file))])

(defn set-query
  [schema-key src-file & args]
  (concat [:set (schema-key (schema src-file))] args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Connector Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord RedisConnector [component])

(defn new-connector
  [component]
  (->RedisConnector component))

(defn setup-schema
  [this]
  )

(defn setup-subscribers
  [this]
  )

(defn add-connection
  [this]
  (assoc (:component this) :conn (config/db-conn (:component this))))

(def connection-behaviour
  {:add-connection add-connection})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dragon Query Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord RedisQuerier [component conn])

(defn pipeline
  "Make one or more calls to the Redis server using the pipeline mechanism:
  ```
  => (pipeline querier [[:get \"foo\"]
                        [:get \"bar\"]
                        [:get \"baz\"]
                        [:get \"quux\"]])
  ```"
  [this lines]
  (car/wcar
    (:conn this)
    :as-pipeline
    (apply car/redis-call lines)))

(defn cmd
  "With this function we can query Redis like the following:

  ```clj
  => (cmd querier [:ping])
  => (cmd querier [:get \"testkey\"])
  => (cmd querier [:set \"foo\" \"bar\"])
  ```

  (Note that the escaped strings are for the docstring, and not what you'd
  actually type.)"
  [this args]
  (let [result (pipeline this [args])]
    (if (= 1 (count result))
      (first result)
      result)))

(defn get-post-category
  [this src-file]
  (cmd (:conn this) (get-query :category src-file)))

(defn get-post-checksum
  [this src-file]
  (cmd (:conn this) (get-query :category src-file)))

(defn get-post-content
  [this src-file]
  (cmd (:conn this) (get-query :category src-file)))

(defn get-post-content-source
  [this src-file]
  (cmd (:conn this) (get-query :category src-file)))

(defn get-post-dates
  [this src-file]
  (cmd (:conn this) (get-query :category src-file)))

(defn get-post-excerpts
  [this src-file]
  (cmd (:conn this) (get-query :category src-file)))

(defn get-post-metadata
  [this src-file]
  (cmd (:conn this) (get-query :category src-file)))

(defn get-post-stats
  [this src-file]
  (cmd (:conn this) (get-query :category src-file)))

(defn get-post-tags
  [this src-file]
  (cmd (:conn this) (get-query :category src-file)))

(defn get-post-uri-path
  [this src-file]
  (cmd (:conn this) (get-query :category src-file)))

(defn get-all-data
  [this src-file]
  (let [data-keys [:category :checksum :content :content-source
                   :dates :excerpts :metadata :stats :tags :uri-path]
        results (pipeline this (mapv #(get-query % src-file) data-keys))]
    (->> results
         (interleave data-keys)
         (partition 2)
         (map vec)
         (into {}))))

(defn get-keys
  [this]
  (cmd (:conn this) [:smembers (:keys (schema))]))

(defn get-n-keys
  [this n order]
  (cmd (:conn this) [:sort (:keys (schema)) :limit 0 n :alpha order]))

(defn get-first-n-keys
  [this n]
  (get-n-keys this n :asc))

(defn get-last-n-keys
  [this n]
  (get-n-keys this n :desc))

(defn get-all-keys
  ([this schema-glob]
    (get-all-keys this schema-glob {}))
  ([this schema-glob opts]
    (let [all-keys (cmd this [:keys schema-glob])]
      (if (:sorted opts)
        (sort all-keys)
        all-keys))))

(defn get-all*
  ([this glob]
    (get-all* this glob {}))
  ([this glob opts]
    (let [results (cmd this
                    (concat [:mget] (get-all-keys this glob opts)))]
      (if (:unique opts)
        (apply set/union results)
        results))))

(defn get-all-tags
  [this]
  (get-all* this "*:tags" {:unique true :sorted true}))

(defn get-tag-freqs
  [this]
  (let [tags (frequencies (mapcat vec (get-all* this "*:tags")))]
    {:tags tags
     :counts (sort #(> (first %1) (first %2))
                   (mapv (fn [[k v]] [v k]) tags))}))

(defn- -get-all-categories
  [this]
  (get-all* this "*:category" {:sorted true}))

(defn get-all-categories
  [this]
  (set (remove #(or (nil? %) (empty? %))
               (-get-all-categories this))))

(defn get-category-freqs
  [this]
  (frequencies (-get-all-categories this)))

(defn get-all-checksums
  [this]
  (get-all* this "*:checksum"))

(defn get-all-stats
  [this]
  (get-all* this "*:stats"))

(defn get-total-char-count
  [this]
  (reduce + (map :char-count (get-all-stats this))))

(defn get-total-line-count
  [this]
  (reduce + (map :line-count (get-all-stats this))))

(defn get-total-word-count
  [this]
  (reduce + (map :word-count (get-all-stats this))))

(defn get-all-metadata
  [this]
  (get-all* this "*:metadata"))

(defn get-all-authors
  [this]
  (set (map :author (get-all-metadata this))))

(defn get-all-posts
  [this]
  ;; XXX probably want to do some custom pipelining for this ...
  )

(defn get-raw
  [this redis-key]
  (cmd (:conn this) :get redis-key))

(defmulti post-changed?
  (fn [this & _]
    (config/db-type (:component this))))

(defn post-changed?
  [this src-file checksum]
  (log/debug "new checksum:" checksum)
  (log/debug "old checksum:" (get-post-checksum this src-file))
  (not= checksum (get-post-checksum this src-file)))

(defn set-keys
  [this src-files]
  (cmd (:conn this) (concat [:sadd (:keys (schema))] src-files)))

(defn set-post-category
  [this src-file category]
  (cmd (:conn this) (set-query :category src-file category)))

(defn set-post-checksum
  [this src-file checksum]
  (cmd (:conn this) (set-query :checksum src-file checksum)))

(defn set-post-content
  [this src-file content]
  (cmd (:conn this) (set-query :content src-file content)))

(defn set-post-content-source
  [this src-file source]
  (cmd (:conn this) (set-query :content-source src-file source)))

(defn set-post-dates
  [this src-file dates]
  (cmd (:conn this) (set-query :dates src-file dates)))

(defn set-post-excerpts
  [this src-file excerpts]
  (cmd (:conn this) (set-query :excerpts src-file excerpts)))

(defn set-post-metadata
  [this src-file metadata]
  (cmd (:conn this) (set-query :metadata src-file metadata)))

(defn set-post-stats
  [this src-file stats]
  (cmd (:conn this) (set-query :stats src-file stats)))

(defn set-post-tags
  [this src-file tags]
  (cmd (:conn this) (set-query :tags src-file tags)))

(defn set-post-uri-path
  [this src-file uri-path]
  (cmd (:conn this) (set-query :uri-path src-file uri-path)))

(defn set-all-checksums
  [this checksum]
  (log/infof "Setting posts checksums to \"%s\" ..." checksum)
  (pipeline this (mapv #(vector :set % checksum)
                       (get-all-checksums this))))

(defn set-all-data
  [this src-file {:keys [category checksum content content-source
                         dates excerpts metadata stats tags uri-path]}]
  (let [data-keys [:category :checksum :content :content-source
                   :dates :excerpts :metadata :stats :tags :uri-path]
        set-vars [category checksum content content-source
                  dates excerpts metadata stats tags uri-path]
        set-data (->> set-vars
                      (interleave data-keys)
                      (partition 2))
        results (pipeline this (mapv (fn [[k v]]
                                       (set-query k src-file v)) set-data))]
    (->> results
         (interleave data-keys)
         (partition 2)
         (map vec)
         (into {}))))

(def query-behaviour
  {:cmd cmd
   :get-keys get-keys
   :get-n-keys get-n-keys
   :get-first-n-keys get-first-n-keys
   :get-last-n-keys get-last-n-keys
   :get-post-category get-post-checksum
   :get-post-checksum get-post-checksum
   :get-post-content get-post-content
   :get-post-content-source get-post-content-source
   :get-post-dates get-post-dates
   :get-post-excerpts get-post-excerpts
   :get-post-metadata get-post-metadata
   :get-post-stats get-post-stats
   :get-post-tags get-post-tags
   :get-post-uri-path get-post-uri-path
   :get-all-authors get-all-authors
   :get-all-categories get-all-categories
   :get-all-data get-all-data
   :get-all-keys get-all-keys
   :get-all-metadata get-all-metadata
   :get-all-tags get-all-tags
   :get-all-stats get-all-stats
   :get-category-freqs get-category-freqs
   :get-tag-freqs get-tag-freqs
   :get-total-char-count get-total-char-count
   :get-total-line-count get-total-line-count
   :get-total-word-count get-total-word-count
   :get-raw get-raw
   :post-changed? post-changed?
   :set-keys set-keys
   :set-post-category set-post-checksum
   :set-post-checksum set-post-checksum
   :set-post-content set-post-content
   :set-post-content-source set-post-content-source
   :set-post-dates set-post-dates
   :set-post-excerpts set-post-excerpts
   :set-post-metadata set-post-metadata
   :set-post-stats set-post-stats
   :set-post-tags set-post-tags
   :set-post-uri-path set-post-uri-path
   :set-all-checksums set-all-checksums
   :set-all-data set-all-data})

(defn new-querier
  [component conn]
  (->RedisQuerier component conn))
