(ns dragon.blog.content.core
  (:require [clojure.string :as string]
            [dragon.blog.content.rfc5322 :as rfc5322]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [taoensso.timbre :as log]
            [trifl.fs :as fs]))

(defn parse-content-type
  [content-type]
  (let [types (string/split content-type #",")
        parts (count types)]
    (if (= 1 parts)
      (keyword content-type)
      (map (comp keyword string/trim) types))))

(defn file-extension-dispatch
  [system content extension]
  (case extension
        :rfc5322 (rfc5322/parse system content)
        :default {:raw-data content
                  :error :parser-not-found}))

(defn parse
  [system file-obj]
  (event/publish system tag/read-source-pre {:file file-obj})
  (let [extension (fs/extension file-obj)
        content (slurp file-obj)]
    (event/publish system tag/read-source-post {:file file-obj})
    (-> extension
        ((partial file-extension-dispatch system content))
        (update-in [:content-type] parse-content-type)
        (assoc :file-type extension))))
