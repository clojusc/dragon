(ns dragon.blog.post.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.blog.post.impl.default :as default]
            [dragon.config.core :as config]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.util :as util])
  (:import (dragon.blog.post.impl.default DefaultBlogPostProcessor)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Protocols   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; Protocols and implementation behaviours are used here in an effort
;;; to provide easily-overridable implementations of the processing steps.

(defprotocol BlogPostProcessorAPI
  (get-data [this data])
  (get-file-data [this data])
  (get-counts [this data])
  (get-link [this data])
  (get-dates [this data])
  (get-tags [this data])
  (get-excerpts [this data])
  (get-body [this data]))

(extend DefaultBlogPostProcessor
        BlogPostProcessorAPI
        default/behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new-processor-fn
  [system]
  (case (config/processor-constructor system)
    :default (fn [] (default/new-processor system))))

(defn new-processor
  [system]
  (default/new-processor system))
