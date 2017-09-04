(ns dragon.blog.post
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.content.core :as content]
            [dragon.util :as util]
            [markdown.core :as markdown]
            [taoensso.timbre :as log]))

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
  (log/debug "Converting post body ...")
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
  (log/debug "Updating tags ...")
  (assoc data :tags (apply sorted-set (string/split (:tags data) #",\s?"))))

(defn add-file-data
  [data]
  (log/debug "Adding file data ...")
  (let [file-obj (:file data)
        file-src (.getPath file-obj)
        filename-old (.getName file-obj)
        filename (format "%s.html" (util/sanitize-str (:title data)))]
    (assoc data
           :filename filename
           :src-file file-src
           :src-dir (.getParent file-obj)
           :uri-path (-> file-src
                         (string/replace filename-old filename)
                         (util/sanitize-post-path)
                         (string/replace-first "posts/" "")))))

(defn add-link
  [uri-base data]
  (log/debug "Adding links ...")
  (let [link-template "<a href=\"%s\">%s</a>"
        url (str uri-base "/" (:uri-path data))
        link (format link-template url (:title data))]
    (assoc data :url url
                :link link)))

(defn add-dates
  [data]
  (log/debug "Adding post dates ...")
  (let [date (util/path->date (:src-file data))
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
      :datestamp datestamp
      :now-timestamp (util/format-timestamp (util/datetime-now))
      :now-datestamp (util/format-datestamp (util/datetime-now)))))

(defn add-counts
  [data]
  (log/debug "Adding counts ...")
  (let [body (:body data)]
    (log/trace "Body data:" data)
    (assoc
      data
      :char-count (util/count-chars body)
      :word-count (util/count-words body)
      :line-count (util/count-lines body))))

(defn add-post-data
  ""
  [data]
  (let [file (:file data)]
    (log/debugf "Adding post data for '%s' ..." file)
    (->> file
         (content/parse)
         (merge data))))

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
