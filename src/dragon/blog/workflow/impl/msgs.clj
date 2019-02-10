(ns dragon.blog.workflow.impl.msgs
  (:require
  	[dragon.event.system.core :as event]
    [dragon.event.tag :as tag]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn send-pre-notification
  [system file-obj]
  ; (event/publish system tag/process-one-pre {:file-obj file-obj})
  file-obj)

(defn send-post-notification
  [system data]
  ; (event/publish->> system tag/process-one-post {:data data})
  data)
