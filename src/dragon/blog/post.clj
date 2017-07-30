(ns dragon.blog.post
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.content.rfc5322 :as rfc5322]
            [dragon.util :as util]
            [markdown.core :as markdown]))

(defn md->html
  [md]
  (markdown/md-to-html-string md :inhibit-separator "%%%"))

(defn join-excerpt
  [words number]
  (let [excerpt (string/join " " (take number words))]
    (if (string/ends-with? excerpt ".")
      (str excerpt "..")
      (str excerpt "..."))))

(defn convert-body
  [data]
  (let [paragraphs (string/split (:body data) #"\n\n")
        words (-> paragraphs
                      (first)
                      (string/split #"\s"))
        words-100 (take 100 words)
        excerpt-100 (join-excerpt words-100 100)
        excerpt-50 (join-excerpt words-100 50)
        excerpt-25 (join-excerpt words-100 25)]
    (case (keyword (:content-type data))
      :md (-> data
              (update-in [:body] md->html)
              (assoc :excerpt-100 (md->html excerpt-100)
                     :excerpt-50 (md->html excerpt-50)
                     :excerpt-25 (md->html excerpt-25))))))

(defn update-tags
  [data]
  (assoc data :tags (apply sorted-set (string/split (:tags data) #",\s?"))))

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
  (let [body (:body data)]
    (assoc
      data
        :char-count (util/count-chars body)
        :word-count (util/count-words body)
        :line-count (util/count-lines body))))

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
