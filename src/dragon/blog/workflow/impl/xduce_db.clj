(ns dragon.blog.workflow.impl.xduce-db
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [dragon.blog.post.core :as post]
    [dragon.components.core :as component-api]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [dragon.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Transducers   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; Functions that return transducers (composed and otherwise) that will be
;;; used to transform post data.

; XXX The following are preserved from a previous implementation, just in case

; (defn ingest-transducer
;   ([system querier]
;     (ingest-transducer system querier default/new-processor))
;   ([system querier processor]
;     (comp
;       (post/process-one-file-data processor)
;       (filter (partial data-source/post-changed? querier))
;       (map (partial data-source/save-post querier))
;       (post/process-one-metadata processor)
;       (post/process-one-content processor))))

; (defn ingest-posts
;   [system processor data]
;   (let [querier (component-api/get-db-querier system)]
;     (into [] (ingest-transducer system querier (processor)) data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord TransducerDBWorkflow [system])

(def behaviour
  {})

(defn new-workflow
  [system]
  (map->TransducerDBWorkflow
    {:system system
     :processor (post/new-processor system)}))
