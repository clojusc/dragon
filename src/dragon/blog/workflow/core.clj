(ns dragon.blog.workflow.core
  "This namespace defines the API for the actions required to run
  blog-generating workflows.

  This is in contrast to the low-level processing of blog content at the data
  source level (see `dragon.blog.post.core`)."
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.blog.post.core :as steps]
            [dragon.blog.workflow.impl.iter :as iterator]
            [dragon.blog.workflow.impl.iter-db :as iterator-db]
            [dragon.blog.workflow.impl.xduce :as transducer]
            [dragon.blog.workflow.impl.xduce-db :as transducer-db]
            [dragon.components.config :as config]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.util :as util])
  (:import (dragon.blog.workflow.impl.iter IteratorWorkflow)
           (dragon.blog.workflow.impl.iter_db IteratorDBWorkflow)
           (dragon.blog.workflow.impl.xduce TransducerWorkflow)
           (dragon.blog.workflow.impl.xduce_db TransducerDBWorkflow)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Protocols   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; Protocols and implementation behaviours are used here in an effort
;;; to provide easily-overridable implementations of the processing steps.

(defprotocol BlogPostWorkflowAPI
  (bust-cache [this])
  (do-file-data-step [this processor] [this processor data])
  (do-metadata-step [this processor] [this processor data])
  (do-content-step [this processor] [this processor data])
  (do-all-steps [this processor] [this processor data])
  (files->data [this file-objs]))

(extend IteratorWorkflow
        BlogPostWorkflowAPI
        iterator/behaviour)

(extend IteratorDBWorkflow
        BlogPostWorkflowAPI
        iterator-db/behaviour)

(extend TransducerWorkflow
        BlogPostWorkflowAPI
        transducer/behaviour)

(extend TransducerDBWorkflow
        BlogPostWorkflowAPI
        transducer-db/behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new-workflow
  [system]
  (case (config/workflow-qualifier system)
    [:iterator :memory] (iterator/new-workflow system)
    [:iterator :db] (iterator-db/new-workflow system)
    [:transducer :memory] (transducer/new-workflow system)
    [:transducer :db] (transducer-db/new-workflow system)))
