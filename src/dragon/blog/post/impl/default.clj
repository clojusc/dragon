(ns dragon.blog.post.impl.default
  (:require
    [clojure.edn :as edn]
    [clojure.string :as string]
    [dragon.blog.content.core :as content]
    [dragon.blog.post.util :as post-util]
    [dragon.components.config :as config]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [dragon.util :as util]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Data Transforms   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-data
  ""
  [this data]
  (let [file (:file data)]
    (log/debugf "Adding post data for '%s' ..." file)
    (->> file
         (content/parse (:system this))
         (merge data)
         (post-util/copy-original-body))))

(defn get-file-data
  [this data]
  (log/debug "Adding file data ...")
  (let [file-obj (:file data)
        file-src (.getPath file-obj)
        filename-old (.getName file-obj)
        filename (format (config/output-file-tmpl (:system this))
                         (util/sanitize-str (:title data)))
        data (dissoc data :file)]
    (assoc data
           :checksum (util/check-sum (pr-str data))
           :filename filename
           :src-file file-src
           :src-dir (.getParent file-obj)
           :uri-path (-> file-src
                         (string/replace filename-old filename)
                         (util/sanitize-post-path)
                         (string/replace-first "posts/" "")))))

(defn get-stats
  [this body]
  (log/debug "Adding stats ...")
  (log/trace "Body data:" body)
  {:char-count (util/count-chars body)
   :word-count (util/count-words body)
   :line-count (util/count-lines body)})

(defn get-link
  [this data]
  (log/debug "Adding post link ...")
  (let [system (:system this)
        url (str (config/posts-path system) "/" (:uri-path data))
        link (format (config/link-tmpl system) url (:title data))]
    (assoc data :url url
                :link link)))

(defn get-dates
  [this src-file]
  (log/debug "Adding post dates ...")
  (let [date (util/path->date src-file)
        timestamp (util/format-timestamp date)
        timestamp-clean (string/replace timestamp #"[^\d]" "")
        datestamp (util/format-datestamp date)]
    {:date date
     :month (util/month->name (:month date))
     :month-short (util/month->short-name (:month date))
     :time (util/format-time date)
     :timestamp timestamp
     :timestamp-long (Long/parseLong timestamp-clean)
     :datestamp datestamp
     :now-timestamp (util/format-timestamp (util/datetime-now))
     :now-datestamp (util/format-datestamp (util/datetime-now))}))

(defn get-tags
  [this tags]
  (log/debug "Updating tags ...")
  (apply sorted-set
         (string/split tags (config/tag-separator (:system this)))))

(defn get-excerpts
  [this body]
  (log/debug "Converting post body ...")
  (let [system (:system this)
        paragraphs (string/split
                    body (config/paragraph-separator system))
        words (-> paragraphs
                  first
                  (string/split (config/word-separator system)))
        words-100 (take 100 words)
        excerpt-100 (post-util/join-excerpt system words-100 100 :as-html)
        excerpt-50 (post-util/join-excerpt system words-100 50 :as-html)
        excerpt-25 (post-util/join-excerpt system words-100 25 :as-html)]
    {:excerpt-100 excerpt-100
     :excerpt-50 excerpt-50
     :excerpt-25 excerpt-25
     :excerpt-100-clean (post-util/scrub-html excerpt-100)
     :excerpt-50-clean (post-util/scrub-html excerpt-50)
     :excerpt-25-clean (post-util/scrub-html excerpt-25)}))

(defn get-body
  [this data]
  (log/debug "Converting post body ...")
  (let [system (:system this)]
    ; (event/publish system tag/parse-content-pre {:body (:body data)})
    (->> data
         :content-type
         (post-util/convert-body! system data)
         ; (event/publish->> system
         ;                   tag/parse-content-post
         ;                   {:body (:body data)})
         )))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord DefaultBlogPostProcessor [system])

(def behaviour
  {:get-data get-data
   :get-stats get-stats
   :get-link get-link
   :get-dates get-dates
   :get-tags get-tags
   :get-excerpts get-excerpts
   :get-body get-body})

(defn new-processor
  [system]
  (->DefaultBlogPostProcessor system))
