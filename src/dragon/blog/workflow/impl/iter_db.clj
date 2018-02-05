(ns dragon.blog.workflow.impl.iter-db
  (:require
    [clojure.java.io :as io]
    [clojure.set :as set]
    [clojure.string :as string]
    [dragon.blog.post.core :as post]
    [dragon.blog.workflow.impl.msgs :as msg]
    [dragon.data.sources.core :as db]
    [dragon.util :as util]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Support Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- get-querier
  [this]
  (get-in this [:system :db :querier]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Iterators   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; An iterative approach to data transformation.

(defn bust-cache
  "Set the checksum for a given post to the empty string. If no `src-file`
  is passed, bust the cache for all posts."
  ([this]
    (log/info "Busting cache for all ...")
    (db/set-posts-checksums (get-querier this) ""))
  ([this src-file]
    (log/debugf "Busting cache for just %s ..." src-file)
    (db/set-post-checksum (get-querier this) src-file "")))

(defn do-file-data-step
  [this init-data]
  (let [processor (:processor this)
        connector (get-in this [:system :db :connector])
        querier (get-in this [:system :db :querier])
        data (->> init-data
                  (post/get-data processor)
                  (post/get-file-data processor))]
    (log/trace "data:" (keys data))
    (log/debug "src-file:" (:src-file data))
    (log/debug "checksum:" (:checksum data))
    data))

(defn- do-save-checksum
  [this querier data]
  (db/set-post-checksum querier (:src-file data) (:checksum data))
  data)

(defn- do-save-file-data
  [this querier data]
  (db/set-file-data querier (:src-file data) data)
  data)

(defn do-metadata-step
  [this data]
  (let [processor (:processor this)]
    (->> data
         (post/get-counts processor)
         (post/get-link processor)
         (post/get-dates processor)
         (post/get-tags processor))))

(defn- do-save-metadata
  [this querier file-data data]
  (let [metadata-keys (vec (set/intersection (set (keys file-data))
                                             (set (keys data))))
        metadata (select-keys data metadata-keys)]
    (db/set-metadata querier (:src-file data) metadata))
  data)

(defn do-content-step
  [this data]
  (let [processor (:processor this)]
    (->> data
         (post/get-excerpts processor)
         (post/get-body processor))))

(defn- do-save-content
  [this querier data]
  (db/set-content querier (:src-file data) (:body data))
  data)

(defn- do-save-all-data
  [this querier data]
  (db/set-all-data querier (:src-file data) data)
  data)

(defn do-all-steps
  [this file-obj]
  (let [system (:system this)
        querier (get-querier this)
        file-data (->> file-obj
                       (msg/send-pre-notification system)
                       (do-file-data-step this))
        post-key (:src-file file-data)]
    (if (db/post-changed? querier file-data)
      (do
        (log/infof "Changed detected; processing %s ..." post-key)
        (let [metadata (->> file-data
                            (do-save-checksum this querier)
                            (do-save-file-data this querier)
                            (do-metadata-step this))]
          (->> metadata
               (do-save-metadata this querier file-data)
               (do-content-step this)
               (do-save-content this querier)
               (do-save-all-data this querier)
               (msg/send-post-notification system)
               (into {}))))
      (do
        (log/debugf "File %s has already been processed; retrieving ..."
                   post-key)
        (db/get-all-data querier post-key)))))

(defn files->data
  [this file-objs]
  (map (partial do-all-steps this) file-objs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord IteratorDBWorkflow [system])

(def behaviour
  {:bust-cache bust-cache
   :do-file-data-step do-file-data-step
   :do-metadata-step do-metadata-step
   :do-content-step do-content-step
   :do-all-steps do-all-steps
   :files->data files->data})

(defn new-workflow
  [system]
  (map->IteratorDBWorkflow
    {:system system
     :processor (post/new-processor system)}))
