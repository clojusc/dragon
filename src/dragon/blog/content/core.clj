(ns dragon.blog.content.core
  (:require
    [clojure.string :as string]
    [dragon.blog.content.rfc5322 :as rfc5322]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [taoensso.timbre :as log]
    [trifl.fs :as fs]))

(defn parse-content-type
  [content-type]
  (log/tracef "Attempting to parse content type '%s' ..." content-type)
  (let [types (string/split content-type #",")
        parts (count types)]
    (if (= 1 parts)
      (keyword content-type)
      (map (comp keyword string/trim) types))))

(defn file-extension-dispatch
  [content extension]
  (case extension
        :rfc5322 (rfc5322/parse content)
        :default {:raw-data content
                  :error :parser-not-found}))

(defn parse
  [file-obj]
  (let [content (slurp file-obj)
        extension (fs/extension file-obj)
        parsed (file-extension-dispatch content extension)]
    (log/trace "Parsed post extension:" extension)
    (log/trace "Raw file data:" content)
    (log/trace "Parsed file data:" parsed)
    (-> parsed
        (update-in [:content-type] parse-content-type)
        (assoc :file-type extension))))
