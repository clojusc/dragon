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
;;; Functions that return transducers (composed and otherwise) that will be;;; used to transform post data.

(defn files->data
  [this file-objs]
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord TransducerWorkflow [system])

(def behaviour
  {:files->data files->data})

(defn new-workflow
  [system]
  (map->TransducerWorkflow
    {:system system
     :processor (post/new-processor system)}))
