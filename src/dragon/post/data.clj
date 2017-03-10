(ns dragon.post.data
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.post.rfc5322 :as rfc5322]
            [dragon.util :as util]
            [markdown.core :as markdown]))

(defn update-tags
  [data]
  (assoc data :tags (string/split (:tags data) #",\s?")))

(defn convert-body
  [data]
  (case (keyword (:content-type data))
    :md (assoc data :body (markdown/md-to-html-string (:body data)))))

(defn add-file-data
  [data]
  (let [file-obj (:file data)
        file-src (.getPath file-obj)
        filename-old (.getName file-obj)
        filename (format "%s.html" (util/sanitize-str (:title data)))]
    (assoc data :filename filename
                :file-src file-src
                :file-dst (-> file-src
                              (string/replace filename-old filename)
                              (string/replace-first "posts" "docs")))))

(defn add-uri
  [data]
  (assoc data :uri (string/replace-first (:file-dst data) "docs/" "")))

(defn add-post-data
  [data]
  (->> data
       :file
       (slurp)
       (rfc5322/parse)
       (merge data)))

(defn process
  ""
  [file-obj]
  (-> file-obj
      (add-post-data)
      (add-file-data)
      (add-uri)
      (convert-body)
      (update-tags)))
