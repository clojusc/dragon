(ns dragon.blog.post
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.content.rfc5322 :as rfc5322]
            [dragon.util :as util]
            [markdown.core :as markdown]))

(defn update-tags
  [data]
  (assoc data :tags (apply sorted-set (string/split (:tags data) #",\s?"))))

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
                :uri-path (-> file-src
                              (string/replace filename-old filename)
                              (string/replace-first "posts/" "")))))

(defn add-link
  [uri-base data]
  (let [link-template "<a href=\"%s\">%s</a>"
        url (str uri-base "/" (:uri-path data))
        link (format link-template url (:title data))]
    (assoc data :url url
                :link link)))

(defn add-dates
  [data]
  (let [date (util/path->date (:file-src data))
        timestamp (util/format-timestamp date)
        timestamp-clean (string/replace timestamp #"[^\d]" "")
        datestamp (util/format-datestamp date)]
    (assoc
      data
        :date date
        :month (util/month->name (:month date))
        :month-short (util/month->short-name (:month date))
        :timestamp timestamp
        :timestamp-long (Long/parseLong timestamp-clean)
        :datestamp datestamp)))

(defn add-counts
  [data]
  (assoc
    data
      :char-count (util/count-chars (:body data))
      :word-count (util/count-words (:body data))))

(defn add-post-data
  ""
  [data]
  (->> data
       :file
       (slurp)
       (rfc5322/parse)
       (merge data)))

(defn process
  ""
  [uri-base file-obj]
  (->> file-obj
       (add-post-data)
       (add-counts)
       (add-file-data)
       (add-link uri-base)
       (add-dates)
       (convert-body)
       (update-tags)))
