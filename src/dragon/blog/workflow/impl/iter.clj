(ns dragon.blog.workflow.impl.iter
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.blog.post.core :as post]
            [dragon.blog.workflow.impl.msgs :as msg]
            [dragon.config.core :as config]
            [dragon.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Iterators   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; An iterative approach to data transformation.

(defn do-file-data-step
  [this data]
  (let [processor (:processor this)]
    (->> data
         (post/get-data processor)
         (post/get-file-data processor))))

(defn do-metadata-step
  [this data]
  (let [processor (:processor this)]
    (->> data
         (post/get-counts processor)
         (post/get-link processor)
         (post/get-dates processor)
         (post/get-tags processor))))

(defn do-content-step
  [this data]
  (let [processor (:processor this)]
    (->> data
         (post/get-excerpts processor)
         (post/get-body processor))))

(defn do-all-steps
  [this file-obj]
  (let [system (:system this)]
    (->> (msg/send-pre-notification system file-obj)
         (do-file-data-step this)
         (do-metadata-step this)
         (do-content-step this)
         (msg/send-post-notification system)
         (into {}))))

(defn files->data
  [this file-objs]
  (map (partial do-all-steps this) file-objs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord IteratorWorkflow [system])

(def behaviour
  {:do-file-data-step do-file-data-step
   :do-metadata-step do-metadata-step
   :do-content-step do-content-step
   :do-all-steps do-all-steps
   :files->data files->data})

(defn new-workflow
  [system]
  (map->IteratorWorkflow
    {:system system
     :processor (post/new-processor system)}))
