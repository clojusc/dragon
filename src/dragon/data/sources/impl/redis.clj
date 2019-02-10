(ns dragon.data.sources.impl.redis
  (:require
    [clojure.java.io :as io]
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
  {:checksum (str path-segment ":checksum")
   :content (str path-segment ":content")
   :dates (str path-segment ":dates")
   :excerpts (str path-segment ":excerpts")
   :metadata (str path-segment ":metadata")
   :category (str path-segment ":category")
   :tags (str path-segment ":tags")
   :stats (str path-segment ":stats")
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

(defrecord RedisQuerier [component])

(defn cmd
  "With this function we can do things like the following in the REPL (for
  querying Redis):

  ```clj
  => (cmd querier 'ping)
  => (cmd querier 'get \"testkey\")
  => (cmd querier 'set \"foo\" \"bar\")
  ```

  (Note that the escaped strings are for the docstring, and not what you'd
  actually type in the REPL.)"
  [this conn cmd & args]
  (log/debug "cmd:" cmd)
  (log/debug "args:" args)
  (log/debug "conn:" conn)
  (log/debug "car/cmr:" (str "car/" cmd))
  (log/trace "symbol of car/cmr:" (symbol (str "car/" cmd)))
  (log/trace "resolved car/cmr:"
             (resolve (symbol (str "taoensso.carmine/" cmd))))
  (car/wcar conn
            (apply (resolve (symbol (str "taoensso.carmine/" cmd))) args)))

(defn new-querier
  [component]
  (->RedisQuerier component))

(defn get-post-category
  [this post-key]
  (cmd (:component this) 'get (:category (schema post-key))))

(defn set-post-category
  [this post-key category]
  (cmd (:component this) 'set (:category (schema post-key)) category))

(defn get-post-checksum
  [this post-key]
  (cmd (:component this) 'get (:checksum (schema post-key))))

(defn set-post-checksum
  [this post-key checksum]
  (cmd (:component this) 'set (:checksum (schema post-key)) checksum))

(defn get-post-content
  [this post-key]
  (cmd (:component this) 'get (:content (schema post-key))))

(defn set-post-content
  [this post-key content]
  (cmd (:component this) 'set (:content (schema post-key)) content))

(defn get-post-dates
  [this post-key]
  (cmd (:component this) 'get (:dates (schema post-key))))

(defn set-post-dates
  [this post-key dates]
  (cmd (:component this) 'set (:dates (schema post-key)) dates))

(defn get-post-excerpts
  [this post-key]
  (cmd (:component this) 'get (:excerpts (schema post-key))))

(defn set-post-excerpts
  [this post-key dates]
  (cmd (:component this) 'set (:excerpts (schema post-key)) dates))

(defn get-post-metadata
  [this post-key]
  (cmd (:component this) 'get (:metadata (schema post-key))))

(defn set-post-metadata
  [this post-key metadata]
  (cmd (:component this) 'set (:metadata (schema post-key)) metadata))

(defn get-post-stats
  [this post-key]
  (cmd (:component this) 'get (:stats (schema post-key))))

(defn set-post-stats
  [this post-key stats]
  (cmd (:component this) 'set (:stats (schema post-key)) stats))

(defn get-post-tags
  [this post-key]
  (cmd (:component this) 'get (:tags (schema post-key))))

(defn set-post-tags
  [this post-key tags]
  (cmd (:component this) 'set (:tags (schema post-key)) tags))

(defn get-post-uri-path
  [this post-key]
  (cmd (:component this) 'get (:uri-path (schema post-key))))

(defn set-post-uri-path
  [this post-key uri-path]
  (cmd (:component this) 'set (:uri-path (schema post-key)) uri-path))

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

(defn get-all-categories
  [this]
  (cmd (:component this) 'get (:categories (schema "all-posts"))))

(defn get-post-keys
  [this schema-glob]
  (sort (cmd (:component this) 'keys schema-glob)))

(defn get-all-tags
  [this]
  (cmd (:component this) 'get (:tags (schema "all-posts"))))

(defn get-all-stats
  [this]
  (cmd (:component this) 'get (:stats (schema "all-posts"))))

(defn get-raw
  [this redis-key]
  (cmd (:component this) 'get redis-key))

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
  (for [checksum-key (cmd (:component this) 'keys "*:checksum")]
    (cmd (:component this) 'set checksum-key checksum)))

(def query-behaviour
  {:cmd cmd
   :get-post-category get-post-checksum
   :get-post-checksum get-post-checksum
   :get-post-content get-post-content
   :get-post-dates get-post-dates
   :get-post-excerpts get-post-excerpts
   :get-post-metadata get-post-metadata
   :get-post-stats get-post-stats
   :get-post-tags get-post-tags
   :get-post-uri-path get-post-uri-path
   :get-post-keys get-post-keys
   :get-all-categories get-all-categories
   :get-all-data get-all-data
   :get-all-tags get-all-tags
   :get-all-stats get-all-stats
   :get-raw get-raw
   :post-changed? post-changed?
   :set-post-category set-post-checksum
   :set-post-checksum set-post-checksum
   :set-post-content set-post-content
   :set-post-dates set-post-dates
   :set-post-excerpts set-post-excerpts
   :set-post-metadata set-post-metadata
   :set-post-stats set-post-stats
   :set-post-tags set-post-tags
   :set-post-uri-path set-post-uri-path
   :set-all-checksums set-all-checksums})
