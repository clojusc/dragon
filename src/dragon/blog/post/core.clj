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
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn send-pre-notification
  [system file-obj]
  (event/publish system tag/process-one-pre {:file-obj file-obj})
  file-obj)

(defn send-post-notification
  [system data]
  (event/publish->> system tag/process-one-post {:data data})
  data)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Processor Protocol   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; Protocols and implementation behaviours are used here in an effort
;;; to provide easily-overridable implementations of the processing steps.

(defprotocol BlogPostProcessorAPI
  (get-post-data [this data])
  (get-post-file-data [this data])
  (get-post-counts [this data])
  (get-post-link [this data])
  (get-post-dates [this data])
  (get-post-tags [this data])
  (get-post-excerpts [this data])
  (get-post-body [this data]))

(extend DefaultBlogPostProcessor
        BlogPostProcessorAPI
        default/behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new-processor-fn
  [system]
  (case (config/processor-constructor system)
    :default default/new-processor))

(defn new-processor
  [system]
  ((new-processor system)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Transducers   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; Composable transformations on post data.

(defn process-one-file-data
  [processor]
  (comp
    (map (partial get-post-data processor))
    (filter util/public?)
    (map (partial get-post-file-data processor))))

(defn process-one-metadata
  [processor]
  (comp
    (map (partial get-post-counts processor))
    (map (partial get-post-link processor))
    (map (partial get-post-dates processor))
    (map (partial get-post-tags processor))))

(defn process-one-content
  [processor]
  (comp
    (map (partial get-post-excerpts processor))
    (map (partial get-post-body processor))))

(defn process-one
  [processor]
  (comp
    (map (partial send-pre-notification (:system processor)))
    (process-one-file-data processor)
    (process-one-metadata processor)
    (process-one-content processor)
    (map (partial send-post-notification (:system processor)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Processes   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; The data transformed

(defn process-one-iter
  ([system file-obj]
    (process-one-iter system default/new-processor file-obj))
  ([system processor-constructor file-obj]
    (let [processor (processor-constructor system)]
      (->> (send-pre-notification system file-obj)
           (get-post-data processor)
           (get-post-counts processor)
           (get-post-file-data processor)
           (get-post-link processor)
           (get-post-dates processor)
           (get-post-tags processor)
           (get-post-excerpts processor)
           (get-post-body processor)
           (into {})
           (send-post-notification system)))))

(defn process-iter
  ([system file-objs]
    (map (partial process-one-iter system) file-objs))
  ([system processor-constructor file-objs]
    (map (partial process-one-iter system processor-constructor) file-objs)))

(defn process-file-data
  ([system file-objs]
    (process-file-data system file-objs default/new-processor))
  ([system file-objs processor-constructor]
    (into [] (process-one-file-data (processor-constructor system)) file-objs)))

(defn process-metadata
  ([system file-objs]
    (process-metadata system file-objs default/new-processor))
  ([system file-objs processor-constructor]
    (into [] (process-one-metadata (processor-constructor system)) file-objs)))

(defn process-content
  ([system file-objs]
    (process-content system file-objs default/new-processor))
  ([system file-objs processor-constructor]
    (into [] (process-one-content (processor-constructor system)) file-objs)))

(defn process
  ([system file-objs]
    (process system file-objs default/new-processor))
  ([system file-objs processor-constructor]
    (into [] (process-one (processor-constructor system)) file-objs)))
