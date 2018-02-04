(ns dragon.blog.workflow.impl.xduce
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [dragon.blog.post.core :as post]
    [dragon.blog.workflow.impl.msgs :as msg]
    [dragon.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Transducers   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; Functions that return transducers (composed and otherwise) that will be
;;; used to transform post data.

(defn do-file-data-step
  [this]
  (let [processor (:processor this)]
    (comp
      (map (partial post/get-data processor))
      (filter util/public?)
      (map (partial post/get-file-data processor)))))

(defn do-metadata-step
  [this]
  (let [processor (:processor this)]
    (comp
      (map (partial post/get-counts processor))
      (map (partial post/get-link processor))
      (map (partial post/get-dates processor))
      (map (partial post/get-tags processor)))))

(defn do-content-step
  [this]
  (let [processor (:processor this)]
    (comp
      (map (partial post/get-excerpts processor))
      (map (partial post/get-body processor)))))

(defn do-all-steps
  [this]
  (let [system (:system this)]
    (comp
      (map (partial msg/send-pre-notification system)))
      (do-file-data-step this)
      (do-metadata-step this)
      (do-content-step this)
      (map (partial msg/send-post-notification system))))

(defn files->data
  [this file-objs]
  (into [] (do-all-steps this) file-objs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord TransducerWorkflow [system])

(def behaviour
  {:do-file-data-step do-file-data-step
   :do-metadata-step do-metadata-step
   :do-content-step do-content-step
   :do-all-steps do-all-steps
   :files->data files->data})

(defn new-workflow
  [system]
  (map->TransducerWorkflow
    {:system system
     :processor (post/new-processor system)}))
