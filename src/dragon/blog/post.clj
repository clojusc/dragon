(ns dragon.blog.post
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.config.core :as config]
            [dragon.blog.content.core :as content]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.util :as util]
            [markdown.core :as markdown]
            [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn md->html
  [system md]
  (markdown/md-to-html-string
   md
   :inhibit-separator (config/template-skip-marker system)))

(defn convert-body
  [system data content-type]
  (case content-type
    :md (update-in data [:body] (partial md->html system))))

(defn join-excerpt
  [system words number]
  (let [excerpt (string/join (config/word-joiner system)
                             (take number words))]
    (if (string/ends-with? excerpt (config/sentence-end system))
      (str excerpt (config/period-ellipsis system))
      (str excerpt (config/ellipsis system)))))

(defn send-pre-notification
  [system file-obj]
  (event/publish system tag/process-one-pre {:file-obj file-obj})
  file-obj)

(defn send-post-notification
  [system data]
  (event/publish->> system tag/process-one-post {:data data})
  data)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Data Transforms   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-post-data
  ""
  [system data]
  (let [file (:file data)]
    (log/infof "Adding post data for '%s' ..." file)
    (->> file
         (content/parse system)
         (merge data))))

(defn get-file-data
  [system data]
  (log/debug "Adding file data ...")
  (let [file-obj (:file data)
        file-src (.getPath file-obj)
        filename-old (.getName file-obj)
        filename (format (config/output-file-tmpl system)
                         (util/sanitize-str (:title data)))]
    (assoc data
           :filename filename
           :src-file file-src
           :src-dir (.getParent file-obj)
           :uri-path (-> file-src
                         (string/replace filename-old filename)
                         (util/sanitize-post-path)
                         (string/replace-first "posts/" "")))))

(defn get-post-counts
  [data]
  (log/debug "Adding counts ...")
  (let [body (:body data)]
    (log/trace "Body data:" data)
    (assoc
      data
      :char-count (util/count-chars body)
      :word-count (util/count-words body)
      :line-count (util/count-lines body))))

(defn get-link
  [system data]
  (log/debug "Adding links ...")
  (let [url (str (config/posts-path system) "/" (:uri-path data))
        link (format (config/link-tmpl system) url (:title data))]
    (assoc data :url url
                :link link)))

(defn get-dates
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

(defn get-tags
  [system data]
  (log/debug "Updating tags ...")
  (assoc data :tags (apply sorted-set (string/split
                                       (:tags data)
                                       (config/tag-separator system)))))

(defn get-converted-body
  [system data]
  (log/debug "Converting post body ...")
  (let [body (:body data)
        paragraphs (string/split
                    body (config/paragraph-separator system))
        words (-> paragraphs
                  (first)
                  (string/split (config/word-separator system)))
        words-100 (take 100 words)
        excerpt-100 (join-excerpt system words-100 100)
        excerpt-50 (join-excerpt system words-100 50)
        excerpt-25 (join-excerpt system words-100 25)]
    (event/publish system tag/parse-content-pre {:body body})
    (-> data
        :content-type
        keyword
        (partial convert-body system data)
        (event/publish-> system tag/parse-content-post {:body (:body data)})
        (assoc :excerpt-100 (md->html system excerpt-100)
               :excerpt-50 (md->html system excerpt-50)
               :excerpt-25 (md->html system excerpt-25)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Transducers   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process-file-data
  [system]
  (comp
    (map (partial get-post-data system))
    (filter util/public?)
    (map (partial get-file-data system))))

(defn process-one
  [system]
  (comp
    (map (partial send-pre-notification system))
    (map (partial get-post-data system))
    (filter util/public?)
    (map (partial get-file-data system))
    (map get-post-counts)
    (map (partial get-link system))
    (map get-dates)
    (map (partial get-tags system))
    (map (partial get-converted-body system))
    (map (partial send-post-notification system))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Processes   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process-one-iter
  ""
  [system file-obj]
  (->> (send-pre-notification system file-obj)
       (get-post-data system)
       (get-post-counts)
       (get-file-data system)
       (get-link system)
       (get-dates)
       (get-tags system)
       (get-converted-body system)
       (into {})
       (send-post-notification system)))

(defn process-iter
  [system file-objs]
  (map (partial process-one-iter system) file-objs))

(defn process
  [system file-objs]
  (into [] (process-one system) file-objs))
