(ns dragon.blog.content.core
  (:require [dragon.blog.content.rfc5322 :as rfc5322]
            [trifl.fs :as fs]))

(defn parse
  [file-obj]
  (let [file-type (fs/extension file-obj)
        content (slurp file-obj)]
    (assoc
     ;; XXX publish pre-parse message
     (case file-type
       :rfc5322 (rfc5322/parse content)
       :default {:raw-data content
                 :error :parser-not-found})
     :file-type file-type)))
