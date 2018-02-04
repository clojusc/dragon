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

(defn schemas
  "For the special case when `path-segment` is the string value `all-posts`,
  the cumulative data for all posts is referenced. This is applicable to
  `categoies`, `tags`, and `stats`."
  [path-segment]
  {:checksum (str path-segment ":checksum")
   :content (str path-segment ":content")
   :file-data (str path-segment ":file-data")
   :metadata (str path-segment ":metadata")
   :categories (str path-segment ":categories")
   :tags (str path-segment ":tags")
   :stats (str path-segment ":stats")
   :all-data (str path-segment ":all-data")})

(defn key->path-segment
  [schema-key]
  (first (string/split schema-key #":")))

(defn cmd
  "With this function we can do things like the following in the REPL (for
  querying Redis):

  ```clj
  => (car/cmd 'ping)
  => (car/cmd 'get \"testkey\")
  => (car/cmd 'set \"foo\" \"bar\")
  ```

  (Note that the escaped strings are for the docstring, and not what you'd
  actually type in the REPL.)"
  [component cmd & args]
  (log/debug "cmd:" cmd)
  (log/debug "args:" args)
  (log/debug "conn:" (:conn (config/db-config component)))
  (log/debug "car/cmr:" (str "car/" cmd))
  (log/trace "symbol of car/cmr:" (symbol (str "car/" cmd)))
  (log/trace "resolved car/cmr:" (resolve (symbol (str "taoensso.carmine/" cmd))))
  (car/wcar (:conn (config/db-config component))
            (apply (resolve (symbol (str "taoensso.carmine/" cmd))) args)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dragon Query Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord RedisQuerier [component])

(defn new-querier
  [component]
  (->RedisQuerier component))

(defn get-post-checksum
  [this post-key]
  (cmd (:component this) 'get (:checksum (schemas post-key))))

(defn get-post-content
  [this post-key]
  (cmd (:component this) 'get (:content (schemas post-key))))

(defn get-post-keys
  ([this]
    (get-post-keys this "*:all-data"))
  ([this schema-glob]
    (sort (cmd (:component this) 'keys schema-glob))))

(defn get-post-metadata
  [this post-key]
  (cmd (:component this) 'get (:metadata (schemas post-key))))

(defn get-post-tags
  [this post-key]
  (cmd (:component this) 'get (:tags (schemas post-key))))

(defn get-post-stats
  [this post-key]
  (cmd (:component this) 'get (:stats (schemas post-key))))

(defn get-all-categories
  [this]
  (cmd (:component this) 'get (:categories (schemas "all-posts"))))

(defn get-all-data
  [this post-key]
  (cmd (:component this) 'get (:all-data (schemas post-key))))

(defn get-all-tags
  [this]
  (cmd (:component this) 'get (:tags (schemas "all-posts"))))

(defn get-all-stats
  [this]
  (cmd (:component this) 'get (:stats (schemas "all-posts"))))

(defn get-raw
  [this redis-key]
  (cmd (:component this) 'get redis-key))

(defmulti post-changed?
  (fn [this & _]
    (config/db-type (:component this))))

(defn post-changed?
  [this post-data]
  (let [post-key (:src-file post-data)
        checksum (:checksum post-data)]
    (log/debug "new checksum:" checksum)
    (log/debug "old checksum:" (get-post-checksum this post-key))
    (not= checksum (get-post-checksum this post-key))))

(defn save-post-checksum
  [this post-data]
  (let [post-key (:src-file post-data)
        checksum (util/check-sum (str post-data))]
    (cmd (:component this)
         'set
         (:checksum (schemas post-key))
         checksum)))

(defn save-post-content
  [this post-data]
  (let [post-key (:src-file post-data)]
    (cmd (:component this)
         'set
         (:content (schemas post-key))
         (:data post-data))))

(defn save-post-metadata
  [this post-data]
  (let [post-key (:src-file post-data)]
    (cmd (:component this)
         'set
         (:metadata (schemas post-key))
         (dissoc post-data :data))))

(defn save-post
  [this post-data]
  (save-post-checksum this post-data)
  (save-post-content this post-data)
  (save-post-metadata this post-data)
  ; (save-post-categories this post-data)
  ; (save-post-tags this post-data)
  ; (save-post-stats this post-data)
  post-data)

(defn set-all-data
  [this post-key data]
  (cmd (:component this) 'set (:all-data (schemas post-key)) data))

(defn set-content
  [this post-key data]
  (cmd (:component this) 'set (:content (schemas post-key)) data))

(defn set-file-data
  [this post-key data]
  (cmd (:component this) 'set (:file-data (schemas post-key)) data))

(defn set-metadata
  [this post-key data]
  (cmd (:component this) 'set (:metadata (schemas post-key)) data))

(defn set-post-checksum
  [this post-key checksum]
  (cmd (:component this) 'set (:checksum (schemas post-key)) checksum))

(defn set-posts-checksums
  [this checksum]
  (log/infof "Setting posts checksums to \"%s\" ..." checksum)
  (for [checksum-key (cmd (:component this) 'keys "*:checksum")]
    (cmd (:component this) 'set checksum-key checksum)))

(def query-behaviour
  {:get-post-checksum get-post-checksum
   :get-post-content get-post-content
   :get-post-keys get-post-keys
   :get-post-metadata get-post-metadata
   :get-post-tags get-post-tags
   :get-post-stats get-post-stats
   :get-all-categories get-all-categories
   :get-all-data get-all-data
   :get-all-tags get-all-tags
   :get-all-stats get-all-stats
   :get-raw get-raw
   :post-changed? post-changed?
   :save-post save-post
   :set-all-data set-all-data
   :set-content set-content
   :set-file-data set-file-data
   :set-metadata set-metadata
   :set-post-checksum set-post-checksum
   :set-posts-checksums set-posts-checksums})
