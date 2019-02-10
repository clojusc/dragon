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
    [trifl.fs :as fs]))

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
  [path-segment]
  {:category (str path-segment ":category")
   :checksum (str path-segment ":checksum")
   :content (str path-segment ":content")
   :content-source (str path-segment ":content-source")
   :dates (str path-segment ":dates")
   :excerpts (str path-segment ":excerpts")
   :metadata (str path-segment ":metadata")
   :stats (str path-segment ":stats")
   :tags (str path-segment ":tags")
   :uri-path (str path-segment ":uri-path")})

(defn key->path-segment
  [schema-key]
  (first (string/split schema-key #":")))

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

(defn cmd
  "With this function we can do things like the following in the REPL (for
  querying Redis):

  ```clj
  => (cmd querier 'ping)
  => (cmd querier :get \"testkey\")
  => (cmd querier :set \"foo\" \"bar\")
  ```

  (Note that the escaped strings are for the docstring, and not what you'd
  actually type in the REPL.)"
  [this args]
  (let [[cmd & cmd-args] args
        driver-fn (resolve (symbol (str "taoensso.carmine/" (name cmd))))
        result (car/wcar (:conn this)
                :as-pipeline
                (apply driver-fn cmd-args))]
    (if (= 1 (count result))
      (first result)
      result)))

(defn get-post-category
  [this post-key]
  (cmd (:conn this) :get (:category (schema post-key))))

(defn set-post-category
  [this post-key category]
  (cmd (:conn this) :set (:category (schema post-key)) category))

(defn get-post-checksum
  [this post-key]
  (cmd (:conn this) :get (:checksum (schema post-key))))

(defn set-post-checksum
  [this post-key checksum]
  (cmd (:conn this) :set (:checksum (schema post-key)) checksum))

(defn get-post-content
  [this post-key]
  (cmd (:conn this) :get (:content (schema post-key))))

(defn set-post-content
  [this post-key content]
  (cmd (:conn this) :set (:content (schema post-key)) content))

(defn get-post-content-source
  [this post-key]
  (cmd (:conn this) :get (:content-source (schema post-key))))

(defn set-post-content-source
  [this post-key source]
  (cmd (:conn this) :set (:content-source (schema post-key)) source))

(defn get-post-dates
  [this post-key]
  (cmd (:conn this) :get (:dates (schema post-key))))

(defn set-post-dates
  [this post-key dates]
  (cmd (:conn this) :set (:dates (schema post-key)) dates))

(defn get-post-excerpts
  [this post-key]
  (cmd (:conn this) :get (:excerpts (schema post-key))))

(defn set-post-excerpts
  [this post-key dates]
  (cmd (:conn this) :set (:excerpts (schema post-key)) dates))

(defn get-post-metadata
  [this post-key]
  (cmd (:conn this) :get (:metadata (schema post-key))))

(defn set-post-metadata
  [this post-key metadata]
  (cmd (:conn this) :set (:metadata (schema post-key)) metadata))

(defn get-post-stats
  [this post-key]
  (cmd (:conn this) :get (:stats (schema post-key))))

(defn set-post-stats
  [this post-key stats]
  (cmd (:conn this) :set (:stats (schema post-key)) stats))

(defn get-post-tags
  [this post-key]
  (cmd (:conn this) :get (:tags (schema post-key))))

(defn set-post-tags
  [this post-key tags]
  (cmd (:conn this) :set (:tags (schema post-key)) tags))

(defn get-post-uri-path
  [this post-key]
  (cmd (:conn this) :get (:uri-path (schema post-key))))

(defn set-post-uri-path
  [this post-key uri-path]
  (cmd (:conn this) :set (:uri-path (schema post-key)) uri-path))

(defn get-all-data
  [this post-key]
  {:category (get-post-category this post-key)
   :checksum (get-post-checksum this post-key)
   :content (get-post-content this post-key)
   :dates (get-post-dates this post-key)
   :excerpts (get-post-excerpts this post-key)
   :metadata (get-post-metadata this post-key)
   :stats (get-post-stats this post-key)
   :tags (get-post-tags this post-key)
   :uri-path (get-post-uri-path this post-key)})

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
  (get-all* this "*:tags" {:unique true}))

(defn get-all-categories
  [this]
  (get-all* this "*:category" {:unique true}))

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

(defn set-all-checksums
  [this checksum]
  (log/infof "Setting posts checksums to \"%s\" ..." checksum)
  (for [checksum-key (cmd (:conn this) 'keys "*:checksum")]
    (cmd (:conn this) :set checksum-key checksum)))

(def query-behaviour
  {:cmd cmd
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
   :get-all-keys get-all-keys
   :get-all-authors get-all-authors
   :get-all-categories get-all-categories
   :get-all-data get-all-data
   :get-all-metadata get-all-metadata
   :get-all-tags get-all-tags
   :get-all-stats get-all-stats
   :get-total-char-count get-total-char-count
   :get-total-line-count get-total-line-count
   :get-total-word-count get-total-word-count
   :get-raw get-raw
   :post-changed? post-changed?
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
   :set-all-checksums set-all-checksums})

(defn new-querier
  [component conn]
  (->RedisQuerier component conn))
