(ns dragon.blog
  (:require [clojure.set :refer [union]]
            [dragon.blog.files :as files]
            [dragon.blog.post :as post]
            [dragon.util :as util]
            [trifl.core :refer [->int]]))

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
      {:month month :posts posts})
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

(defn get-posts
  []
  (map (fn [x]
         {:file x})
       (util/get-files "posts")))

(defn process
  [uri-base]
  (->> (get-posts)
       (map (partial post/process uri-base))
       (sort compare-timestamp)))

(defn tags
  [data]
  (->> data
       (map :tags)
       (apply union)))

(defn data-for-archives
  [data]
  (->> data
       (group-by-year)
       (map group-year-by-month)))

(defn data-for-categories
  [data]
  (->> data
       (group-by-category)
       (map update-category-groups)
       (sort compare-category)))

(defn data-for-tags
  [data]
  (let [all-tags (tags data)]
    (map (partial group-by-tag data) all-tags)))

(defn data-for-authors
  [data]
  (->> data
       (group-by-author)
       (map update-author-groups)
       (sort compare-author)))

(defn get-archive-routes
  [data & {:keys [uri-base gen-func]}]
  (let [route (format "%s/%s" uri-base (:uri-path data))
        data (gen-func data)]
    (->> data
         (data-for-archives)
         [route data]
         (into {}))))
