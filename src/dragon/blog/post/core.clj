(ns dragon.blog.post.core
  "This namespace defines the API for the actions required to process blog
  content sources at a low level.

  One of any number of processor implementations (see the namespaces
  `dragon.blog.post.impl.*`) are configured for use in a blog's `project.clj`
  file via `:dragon` -> `:processor` -> `:constructor`. The configured instance
  is then used by the high-level API to execute configured content-processing
  workflows.

  For high-level processing, i.e., 'workflow', see the following namespaces:
  ```
  * dragon.blog.workflow.core
  * dragon.blog.workflow.impl.*
  ```"
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [dragon.blog.post.impl.default :as default]
    [dragon.components.config :as config]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [dragon.util :as util])
  (:import
    (dragon.blog.post.impl.default DefaultBlogPostProcessor)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Protocols   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; Protocols and implementation behaviours are used here in an effort
;;; to provide easily-overridable implementations of the processing steps.

(defprotocol BlogPostProcessorAPI
  (get-data [this data template-config])
  (get-dates [this data])
  (get-excerpts [this data])
  (get-stats [this data])
  (get-tags [this data tag-separator])
  (process-file [this querier file data opts]))

(extend DefaultBlogPostProcessor
        BlogPostProcessorAPI
        default/behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new-processor
  [system]
  (case (config/processor-constructor system)
    :default (default/new-processor system)))
