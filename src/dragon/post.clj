(ns dragon.post
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.post.rfc5322 :as rfc5322]
            [dragon.util :as util]
            [markdown.core :as markdown]))

(defn get-all
  []
  (map (fn [x] {:file x}) (util/get-files "posts")))

(defn ->dst-path
  [src-path title]
  (let [filename (.getName src-path)]
    (-> src-path
        (string/replace-first "posts" "docs")
        (string/replace-first filename title))))

;; get new names:
;; (map (comp (fn [x] (str x "/title.html")) post/->dst-path (fn [x] (.getParent x))) (post/get-all))

;; format dates:
;; (map (comp util/format-date util/path->date (fn [x] (.getPath x))) (post/get-all))

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

(defn process-files
  ""
  [data]
  ;; XXX write post to archives
  ;; XXX update archives year page with link
  ;; XXX update category page with link
  ;; XXX update tags page(s) with link
  ;; XXX update authors page with link
  ;; XXX update front page
  )

(defn process-data
  ""
  [file-obj]
  (-> file-obj
      (add-post-data)
      (add-file-data)
      (add-uri)
      (convert-body)
      (update-tags)))

(defn process
  [file-obj]
  (-> file-obj
      (process-data)
      (process-files)))

(defn process-all
  []
  (map process (get-all)))
