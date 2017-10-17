(ns dragon.blog.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.blog.post :as post]
            [dragon.blog.tags :as tags]
            [dragon.config :as config]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.util :as util]
            [taoensso.timbre :as log]
            [trifl.core :refer [->int]]
            [trifl.fs :as fs])
  (:import (java.io.File)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def legal-post-file-extensions
  #{".rfc5322"})

(defn legal-content-file?
  [^java.io.File post]
  (->> legal-post-file-extensions
       (map #(string/ends-with? (.getCanonicalPath post) %))
       (remove false?)
       (not-empty)))

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
         (map :file))))

(defn ingest-post
  [system data]
  ;; XXX need to update dragon.data.sources to use protocols before using this
  ;(when (db/post-changed? system data)
  ;  )
  )

(defn process
  [system]
  (log/debug "Processing posts ...")
  (event/publish system tag/process-all-pre)
  (let [raw-posts (get-posts system)
        processed-posts (post/process system raw-posts)]
    ;; XXX maybe doall here instead of using vec to realize?
    (->> processed-posts
         (sort compare-timestamp-desc)
         (event/publish->> system
                           tag/process-all-post
                           {:count (count processed-posts)})
         vec)))

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
    (log/infof "Generating route for %s ..." route)
    (log/debugf "This index: %s (prev: %s; next: %s)"
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
  (log/trace "Got data:" data)
  (->> data
       (map (partial get-indexed-archive-route uri-base gen-func data))
       (into {})))
