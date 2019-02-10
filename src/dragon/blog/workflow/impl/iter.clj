(ns dragon.blog.workflow.impl.iter
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [dragon.blog.post.core :as post]
    [dragon.blog.workflow.impl.msgs :as msg]
    [dragon.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Iterators   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; An iterative approach to data transformation.

(defn files->data
  [this file-objs]
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord IteratorWorkflow [system])

(def behaviour
  {:files->data files->data})

(defn new-workflow
  [system]
  (map->IteratorWorkflow
    {:system system
     :processor (post/new-processor system)}))
