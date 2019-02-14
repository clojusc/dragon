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

(defn schema
  "This function retuns the Redis 'schemas' (for lack of a better word) for a
  given path-segment (blog post key). If a key is not passed, the implication
  is that blog-wide, non-post-specific data is being referenced."
  ([]
    {:text-stats "all-text-stats"
     :category-stats "all-category-stats"
     :keys "path-segments"
     :tag-stats "all-tag-stats"})
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
  [schema-key & [src-file]]
  (if (nil? src-file)
    [:get (schema-key (schema))]
    [:get (schema-key (schema src-file))]))

(defn set-query
  [schema-key & args]
  (if (= 1 (count args))
    (concat [:set (schema-key (schema))] args)
    (let [[src-file & args] args]
      (concat [:set (schema-key (schema src-file))] args))))

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
  ([this nth order]
    (get-n-keys this 0 nth order))
  ([this offset count order]
    (cmd (:conn this) [:sort
                       (:keys (schema))
                       :limit offset count
                       :alpha order])))

(defn get-first-n-keys
  ([this nth]
    (get-first-n-keys this 0 nth))
  ([this offset count]
    (get-n-keys this offset count :asc)))

(defn get-last-n-keys
  ([this nth]
    (get-last-n-keys this 0 nth))
  ([this offset count]
    (get-n-keys this offset count :desc)))

(defn get-all*
  ([this ^Keyword schema-key]
    (get-all* this schema-key {}))
  ([this ^Keyword schema-key opts]
    (let [results (cmd this
                       (concat [:mget]
                              (mapv #(str % schema-key)
                                    (get-keys this))))]
      (if (:unique opts)
        (apply set/union results)
        results))))

(defn get-all-tags
  [this]
  (get-all* this :tags {:unique true :sorted true}))

(defn get-tag-freqs
  [this]
  (let [tags (frequencies (mapcat vec (get-all* this :tags)))]
    {:tags tags
     :counts (sort util/compare-first
                   (util/invert-tuple tags))}))

(defn get-tag-totals
  [this]
  (reduce + (map first (:counts (get-tag-freqs this)))))

(defn get-tag-max-count
  [this]
  (apply max (map first (:counts (get-tag-freqs this)))))

(defn get-tag-stats
  [this]
  (cmd (:conn this) (get-query :tag-stats)))

(defn- -get-all-categories
  [this]
  (sort (get-all* this :category)))

(defn get-all-categories
  [this]
  (remove
    util/nada?
    (-get-all-categories this)))

(defn get-category-freqs
  [this]
  (let [cats (frequencies (get-all-categories this))]
    {:categories cats
     :counts (sort util/compare-first
                   (util/invert-tuple cats))}))

(defn get-category-totals
  [this]
  (reduce + (map first (:counts (get-category-freqs this)))))

(defn get-category-max-count
  [this]
  (apply max (map first (:counts (get-category-freqs this)))))

(defn get-category-stats
  [this]
  (cmd (:conn this) (get-query :category-stats)))

(defn get-all-checksums
  [this]
  (get-all* this :checksum))

(defn get-all-stats
  [this]
  (get-all* this :stats))

(defn get-total-char-count
  [this]
  (reduce + (map :char-count (get-all-stats this))))

(defn get-total-line-count
  [this]
  (reduce + (map :line-count (get-all-stats this))))

(defn get-total-word-count
  [this]
  (reduce + (map :word-count (get-all-stats this))))

(defn get-text-stats
  [this]
  (cmd (:conn this) (get-query :text-stats)))

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

(defn set-category-stats
  [this stats]
  (cmd (:conn this) (set-query :category-stats stats)))

(defn set-tag-stats
  [this stats]
  (cmd (:conn this) (set-query :tag-stats stats)))

(defn set-text-stats
  [this stats]
  (cmd (:conn this) (set-query :text-stats stats)))

(def query-behaviour
  {:cmd cmd
   :get-all-authors get-all-authors
   :get-all-categories get-all-categories
   :get-all-data get-all-data
   :get-all-metadata get-all-metadata
   :get-all-stats get-all-stats
   :get-all-tags get-all-tags
   :get-category-freqs get-category-freqs
   :get-category-max-count get-category-max-count
   :get-category-stats get-category-stats
   :get-category-totals get-category-totals
   :get-first-n-keys get-first-n-keys
   :get-keys get-keys
   :get-last-n-keys get-last-n-keys
   :get-n-keys get-n-keys
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
   :get-raw get-raw
   :get-tag-freqs get-tag-freqs
   :get-tag-max-count get-tag-max-count
   :get-tag-stats get-tag-stats
   :get-tag-totals get-tag-totals
   :get-text-stats get-text-stats
   :get-total-char-count get-total-char-count
   :get-total-line-count get-total-line-count
   :get-total-word-count get-total-word-count
   :post-changed? post-changed?
   :set-all-checksums set-all-checksums
   :set-all-data set-all-data
   :set-category-stats set-category-stats
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
   :set-tag-stats set-tag-stats
   :set-text-stats set-text-stats})

(defn new-querier
  [component conn]
  (->RedisQuerier component conn))
