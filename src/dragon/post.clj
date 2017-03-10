(ns dragon.post
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.post.data :as data]
            [dragon.post.files :as files]
            [dragon.util :as util]
            [markdown.core :as markdown]))

(defn get-all
  []
  (map (fn [x] {:file x}) (util/get-files "posts")))

(defn process
  [file-obj]
  (-> file-obj
      (data/process)
      (files/process)))

(defn process-all
  []
  (map process (get-all)))
