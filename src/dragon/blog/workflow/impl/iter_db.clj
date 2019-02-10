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
    (db/set-all-checksums (get-querier this) ""))
  ([this src-file]
    (log/debugf "Busting cache for just %s ..." src-file)
    (db/set-post-checksum (get-querier this) src-file "")))

(defn files->data
  [this file-objs]
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord IteratorDBWorkflow [system])

(def behaviour
  {:bust-cache bust-cache
   :files->data files->data})

(defn new-workflow
  [system]
  (map->IteratorDBWorkflow
    {:system system
     :processor (post/new-processor system)}))
