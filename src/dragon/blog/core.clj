(ns dragon.blog.core
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [dragon.blog.post.core :as post]
    [dragon.blog.post.impl.default :as default]
    [dragon.blog.tags :as tags]
    [dragon.components.config :as config]
    [dragon.components.db :as db-component]
    [dragon.data.sources.core :as db]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [dragon.util :as util]
    [taoensso.timbre :as log]
    [trifl.core :refer [->int]]
    [trifl.fs :as fs])
  (:import
    (java.io File)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def legal-post-file-extensions
  #{".rfc5322"})

(defn legal-content-file?
  [^File post]
  (log/trace "Checking legality of file:" post)
  (->> legal-post-file-extensions
       (map #(string/ends-with? (.getCanonicalPath post) %))
       (remove false?)
       (not-empty)
       ((fn [x] (log/tracef "Found file: %s" x) x))))

(defn post-url
  [uri-base post]
  ;; XXX maybe use config function to get uri-based instead of passing it?
  (format "%s/%s" uri-base (:uri-path post)))

(defn data-for-logs
  [data]
  (log/trace "Dropping body from: " (vec data))
  (cond
    (map? data) (assoc data :body "...")
    (coll? data) (map #(assoc % :body "...") data)))

(defn compare-timestamp-desc
  [a b]
  (< (:timestamp-long b)
     (:timestamp-long a)))

(defn compare-timestamp
  [a b]
  (< (:timestamp-long a)
     (:timestamp-long b)))

(defn compare-category
  [a b]
  (compare (:category a)
           (:category b)))

(defn compare-author
  [a b]
  (compare (:author a)
           (:author b)))

(defn group-by-year
  [data]
  (group-by
    #(->int (get-in % [:date :year]))
    data))

(defn group-by-month
  [data]
  (map
    (fn [[month posts]]
      {:month (util/month->name month) :posts posts})
    (group-by
      #(->int (get-in % [:date :month]))
      data)))

(defn group-by-category
  [data]
  (group-by :category data))

(defn group-by-tag
  [data tag]
  {:tag tag
   :posts (filter #(contains? (:tags %) tag) data)})

(defn group-by-author
  [data]
  (group-by :author data))

(defn group-year-by-month
  [[year-key  year-data]]
  {:year year-key
   :months (group-by-month year-data)})

(defn update-category-groups
  [[cat-key cat-data]]
  {:category cat-key
   :posts cat-data})

(defn update-author-groups
  [[auth-key auth-data]]
  {:author auth-key
   :posts auth-data})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Core Processing Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-files
  [dir]
  (->> dir
       (io/file)
       (file-seq)
       (filter fs/file?)
       (filter legal-content-file?)))

(defn get-posts
  [system]
  (let [posts-path (config/posts-path-src system)]
    (log/debugf "Finding posts under '%s' dir ..." posts-path)
    (->> posts-path
         (get-files)
         ((fn [x] (log/debugf "Found %s files ..." (count x)) x))
         (map #(hash-map :file %)))))

(defn process
  [system]
  (doseq [file (sort (get-files (config/posts-path-src system)))]
    (let [src-file (.getPath file)
          _ (log/infof "Checking source file %s ..." src-file)
          processor (post/new-processor system)
          querier (db-component/db-querier system)
          tmpl-cfg (config/template-config system)
          data (post/get-data processor file tmpl-cfg)
          checksum (util/check-sum (pr-str data))
          filename (format (config/output-file-tmpl system)
                           (util/sanitize-str (:title data)))
          opts {:tag-separator (config/tag-separator system)
                :checksum checksum
                :src-file src-file
                :filename filename}]
      (log/debug "Got checksum:" (:checksum opts))
      (log/debug "Got filename:" (:filename opts))
      (if (db/post-changed? querier src-file checksum)
        (db/set-post-data
          querier
          src-file
          (post/process-file processor querier file data opts))
        (log/infof "File %s has already been processed; skipping ..."
                   src-file))))
  :ok)

(defn reset-content-checksums
  [system]
  ;; XXX re-implement without workflow ns ...
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Core Grouping Multimethods   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti group-data
  (fn [type _] type))

(defmethod group-data :archives
  [_type data]
  (->> data
       (group-by-year)
       (map group-year-by-month)))

(defmethod group-data :categories
  [_type data]
  (->> data
       (group-by-category)
       (map update-category-groups)
       (sort compare-category)))

(defmethod group-data :tags
  [_type data]
  (let [unique-tags (tags/unique data)]
    (map (partial group-by-tag data) unique-tags)))

(defmethod group-data :authors
  [_type data]
  (->> data
       (group-by-author)
       (map update-author-groups)
       (sort compare-author)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Route-generating Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-archive-route
  [uri-base gen-func post-data]
  (let [route (post-url uri-base post-data)]
    (log/infof "Generating route for %s ..." route)
    [route (gen-func post-data)]))

(defn get-archive-routes
  [data & {:keys [uri-base gen-func]}]
  (log/trace "Got data:" (data-for-logs data))
  (->> data
       (map (partial get-archive-route uri-base gen-func))
       (into {})))

(defn get-indexed-archive-route
  [uri-base gen-func posts [post-idx post-data]]
  ;; XXX get uri-base from configuration; don't pass
  (let [route (post-url uri-base post-data)
        len (count posts)
        prev-idx (when-not (= post-idx (dec len)) (inc post-idx))
        next-idx (when-not (zero? post-idx) (dec post-idx))]
    (log/debugf "Indexing route for %s ..." route)
    (log/tracef "This index: %s (prev: %s; next: %s)"
                post-idx prev-idx next-idx)
    [route
     (gen-func
       (assoc
         post-data
         :prev-post (when prev-idx
                      (post-url uri-base (second (nth posts prev-idx))))
         :next-post (when next-idx
                      (post-url uri-base (second (nth posts next-idx))))))]))

(defn get-indexed-archive-routes
  [data & {:keys [uri-base gen-func]}]
  (log/info "Indexing routes ...")
  (log/trace "Got data:" data)
  (->> data
       (map (partial get-indexed-archive-route uri-base gen-func data))
       (into {})))
