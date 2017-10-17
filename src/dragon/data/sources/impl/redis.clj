(ns dragon.data.sources.impl.redis
  (:require [clojure.java.io :as io]
            [dragon.config :as config]
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
   :metadata (str path-segment ":metadata")
   :categories (str path-segment ":categories")
   :tags (str path-segment ":tags")
   :stats (str path-segment ":stats")})

(defn cmd
  "With this function we can do things like the following in the REPL (for
  querying Redis):

  ```clj
  => (redis/cmd 'ping)
  => (redis/cmd 'get \"testkey\")
  => (redis/cmd 'set \"foo\" \"bar\")
  ```

  (Note that the escaped strings are for the docstring, and not what you'd
  actually type in the REPL.)"
  [component-or-system cmd & args]
  (car/wcar (:conn (config/db-config component-or-system))
            (apply (resolve (symbol (str "car/" cmd))) args)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dragon Query API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-post-checksum
  [this post-key]
  (cmd (:component this) 'get (:checksum (schemas post-key))))

(defn get-post-content
  [this post-key]
  (cmd (:component this) 'get (:content (schemas post-key))))

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

(defn get-all-tags
  [this]
  (cmd (:component this) 'get (:tags (schemas "all-posts"))))

(defn get-all-stats
  [this]
  (cmd (:component this) 'get (:stats (schemas "all-posts"))))

(defmulti post-changed?
  (fn [this & _] (config/db-type (:component this))))

(defn post-changed?
  [this post-data]
  (let [post-key (:uri-path post-data)
        checksum (util/check-sum (str post-data))]
    (not (= checksum (get-post-checksum this post-key)))))

(defrecord RedisQuerier [component])

(defn new-querier
  [component]
  (->RedisQuerier component))

(def query-behaviour
  {:get-post-checksum get-post-checksum
   :get-post-content get-post-content
   :get-post-metadata get-post-metadata
   :get-post-tags get-post-tags
   :get-post-stats get-post-stats
   :get-all-categories get-all-categories
   :get-all-tags get-all-tags
   :get-all-stats get-all-stats
   :post-changed? post-changed?})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dragon Connection API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord RedisConnector [component])

(defn new-connector
  [component]
  (->RedisConnector component))

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
      (do (->> (slurp id-file)
               (util/shell! "docker" "stop")
               vec)
          (util/shell! "rm" id-file)))))

(def connection-behaviour
  {:start-db! start-db!
   :add-connection add-connection
   :stop-db! stop-db!})
