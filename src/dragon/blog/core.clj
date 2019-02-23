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
  ;; XXX maybe use config function to get uri-base instead of passing it?
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
  [[key data]]
  {:year key
   :months (group-by-month data)})

(defn update-category-groups
  [[key data]]
  {:category key
   :posts data})

(defn update-author-groups
  [[key data]]
  {:author key
   :posts data})

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
  (sort (get-files (config/posts-path-src system))))

(defn process-file
  [system processor querier file]
  (let [src-file (.getPath file)
        _ (log/infof "Checking source file %s ..." src-file)
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
      (let [processed-data (post/process-file processor file data opts)
            author (get-in processed-data [:metadata :author])
            cat (:category processed-data)
            year (get-in processed-data [:dates :date :year])]
        (log/debug "Got author: " author)
        (log/debug "Got category: " cat)
        (log/debug "Got year: " year)
        (db/set-all-data querier src-file processed-data)
        ;; The following inserts will be used later in various group-by
        ;; queries.
        (db/pipeline
          querier
          (concat
            [(db/set-add-query querier :author-posts author src-file)
             (db/set-add-query querier :category-posts cat src-file)
             (db/set-add-query querier :year-posts year src-file)]
            (mapv #(db/set-add-query querier :tag-posts % src-file)
                  (:tags processed-data)))))
      (log/infof "File %s has already been processed; skipping ..."
                 src-file))))

(defn process-files
  [system]
  (let [files (get-posts system)
        processor (post/new-processor system)
        querier (db-component/db-querier system)
        added (db/set-keys querier (mapv #(.getPath %) files))]
    (log/infof "Added %s new blog post keys ..." added)
    (doseq [file files]
      (process-file system processor querier file)))
  :ok)

(defn get-ratios
  ([data max factor]
    (->> data
         (map (fn [[k v]]
               [k (Math/round (* factor (float (/ v max))))]))
         (into {})))
  ([data max factor mode]
    (case mode
      :inverted (->> (get-ratios data max factor)
                     (map (fn [[k v]] [k (- factor v)]))
                     (into {})))))

(defn get-stats
  [freqs-segment total max]
  {:five (get-ratios freqs-segment max 5)
   :five-inverted (get-ratios freqs-segment max 5 :inverted)
   :hundred (get-ratios freqs-segment max 100)
   :hundred-inverted (get-ratios freqs-segment max 100 :inverted)
   :max max
   :percent (get-ratios freqs-segment total 100)
   :percent-inverted (get-ratios freqs-segment total 100 :inverted)
   :ten (get-ratios freqs-segment max 10)
   :ten-inverted (get-ratios freqs-segment max 10 :inverted)
   :total total})

(defn process-category-stats
  [system]
  (log/info "Adding blog-wide category stats to db ...")
  (let [querier (db-component/db-querier system)
        total (db/get-category-totals querier)
        max (db/get-category-max-count querier)
        freqs (db/get-category-freqs querier)]
    (db/set-category-stats
      querier
      (merge freqs (get-stats (:categories freqs) total max)))))

(defn process-tag-stats
  [system]
  (log/info "Adding blog-wide tag stats to db ...")
  (let [querier (db-component/db-querier system)
        total (db/get-tag-totals querier)
        max (db/get-tag-max-count querier)
        freqs (db/get-tag-freqs querier)]
    (db/set-tag-stats
      querier
      (merge freqs (get-stats (:tags freqs) total max)))))

(defn process-text-stats
  [system]
  (log/info "Adding blog-wide textual stats to db ...")
  (let [querier (db-component/db-querier system)
        data {:chars (db/get-total-char-count querier)
              :lines (db/get-total-line-count querier)
              :words (db/get-total-word-count querier)}]
    (db/set-text-stats querier data)))

(defn process
  [system]
  ;; Iterate through files on filesystem -- sources of truth
  (process-files system)
  ;; Post-process: look at all posts and extract stats
  (process-text-stats system)
  (process-category-stats system)
  (process-tag-stats system))

(defn reset-content-checksums
  [system]
  (db/set-all-checksums
    (db-component/db-querier system)
    "invalidated")
  :ok)

(defn reset-listing-data
  [system]
  (let [querier (db-component/db-querier system)]
      (db/pipeline
        querier
        (concat
          (mapv #(db/del-query querier :author-posts %)
                (db/get-all-authors querier))
          (mapv #(db/del-query querier :category-posts %)
                (db/get-all-categories querier))
          (mapv #(db/del-query querier :tag-posts %)
                (db/get-all-tags querier))
          (mapv #(db/del-query querier :year-posts %)
                (map #(get-in % [:date :year]) (db/get-all-dates querier)))))))

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
  [_type system]
  (->> system
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
