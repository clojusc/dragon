(ns dragon.post.files
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.util :as util]))

(defn write-post
  [data]
  data)

(defn update-archives
  [data]
  data)

(defn update-category
  [data]
  data)

(defn update-tags
  [data]
  data)

(defn update-authors
  [data]
  data)

(defn update-front-page
  [data]
  data)

(defn process
  ""
  [data]
  (-> data
      (write-post)
      (update-archives)
      (update-category)
      (update-tags)
      (update-authors)
      (update-front-page)))
