(ns dragon.data.core
  (:require [dragon.event.tag :as tag]
            [taoensso.timbre :as log]))

(def default-subscribed-tags
  [tag/subscribers-added
   tag/process-all-pre
   tag/process-all-post
   tag/run-cli
   tag/read-source-pre
   tag/read-source-post
   tag/parse-file-pre
   tag/parse-file-post
   tag/parse-content-pre
   tag/parse-content-post
   tag/write-output-pre
   tag/write-output-post
   tag/generate-routes-pre
   tag/generate-routes-post
   tag/shutdown-cli])
