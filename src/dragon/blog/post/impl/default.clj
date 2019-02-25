(ns dragon.blog.post.impl.default
  (:require
    [clojure.edn :as edn]
    [clojure.string :as string]
    [dragon.blog.content.core :as content]
    [dragon.blog.post.util :as post-util]
    [dragon.components.config :as config]
    [dragon.components.db :as db-component]
    [dragon.data.sources.core :as db]
    [dragon.data.sources.impl.redis :refer [schema]]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [dragon.util :as util]
    [taoensso.timbre :as log])
  (:import
    (java.util Random)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Data Transforms   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-data
  [this file-obj tmpl-cfg]
  (log/debug "Getting data from file ...")
  (let [data (content/parse file-obj)]
    (post-util/convert-body!
      (post-util/copy-original-body data)
      tmpl-cfg)))

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

(defn get-stats
  [this body]
  (log/debug "Adding stats ...")
  (log/trace "Body data:" body)
  {:char-count (util/count-chars body)
   :word-count (util/count-words body)
   :line-count (util/count-lines body)})

(defn get-tags
  [this tags separator]
  (log/debug "Updating tags ...")
  (apply sorted-set
         (->> (string/split tags separator)
              (map #'string/trim)
              (remove #(or (nil? %) (empty? %))))))

(defn random-text-idx
  "Generate a random number given input text as a seed and a max range."
  [text max-count]
  (inc (.nextInt (new Random (.hashCode text)) max-count)))

(defn get-image-path
  [this suffix]
  (format "%s/img/%s"
          (config/base-path (:system this))
          suffix))

(defn get-default-image
  [this cfg-tmpl-fn title]
  (let [img-count (config/default-images-count (:system this))
        img-idx (random-text-idx title img-count)]
    (format (get-image-path this (cfg-tmpl-fn (:system this)))
            img-idx)))

(defn get-image
  [this explicit-img cfg-tmpl-fn title]
  (if-not (util/nada? explicit-img)
     (get-image-path this explicit-img)
     (get-default-image this cfg-tmpl-fn title)))

(defn get-images
  [this data]
  (let [title (:title data)
        explicit-header-image (:header-image data)
        explicit-headliner-image (:headliner-image data)
        explicit-small-image (:small-image data)
        explicit-thumb-image (:thumbnail-image data)
        header-image (get-image this
                                explicit-header-image
                                config/default-images-post-tmpl
                                title)
        headliner-image (get-image this
                                   explicit-headliner-image
                                   config/default-images-headliner-tmpl
                                   title)
        small-image (get-image this
                               explicit-small-image
                               config/default-images-small-tmpl
                               title)
        thumb-image (get-image this
                               explicit-thumb-image
                               config/default-images-thumb-tmpl
                               title)]
    ;; XXX Add logic for generating headliner, small, and thumb from an
    ;;     explicit header image, if given. Will need to use an image lib
    ;;     or cli tool.
    {:header header-image
     :headliner headliner-image
     :small small-image
     :thumb thumb-image}))

(defn process-file
  [this file data opts]
  (log/infof "Changed detected; processing %s ..." (:src-file opts))
  (let [src-dir (.getParent file)
        filename-old (.getName file)
        metadata (dissoc data :body :body-orig :tags :category)
        uri-path (-> (:src-file opts)
                     (string/replace filename-old (:filename opts))
                     (util/sanitize-post-path)
                     (string/replace-first "posts/" ""))
        tags (get-tags this (:tags data) (:tag-separator opts))
        dates (get-dates this (:src-file opts))
        stats (get-stats this (:body data))
        excerpts (get-excerpts this (:body data))
        images (get-images this data)]
    (log/trace "Got data:" data)
    (log/trace "Got dates:" dates)
    (log/trace "Got excerpts:" excerpts)
    (log/trace "Got excerpts:" excerpts)
    (log/debug "Got images:" images)
    (log/trace "Got src-dir:" src-dir)
    (log/trace "Got stats:" stats)
    (log/trace "Got tags:" tags)
    (log/infof "Post %s will be accessible at: %s" (:title metadata) uri-path)
    {:category (string/trim (:category data))
     :checksum (:checksum opts)
     :content (:body data)
     :content-source (:body-orig data)
     :dates dates
     :excerpts excerpts
     :images images
     :metadata metadata
     :stats stats
     :tags tags
     :uri-path uri-path}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord DefaultBlogPostProcessor [system])

(def behaviour
  {:get-data get-data
   :get-dates get-dates
   :get-excerpts get-excerpts
   :get-stats get-stats
   :get-tags get-tags
   :process-file process-file})

(defn new-processor
  [system]
  (->DefaultBlogPostProcessor system))
