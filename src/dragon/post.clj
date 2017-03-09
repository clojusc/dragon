(ns dragon.post
  (:require [clojure.string :as string]
            [dragon.util :as util]))

(defn get-all
  []
  (util/get-files "posts"))

(defn ->dst-path
  [src-path]
  (-> src-path
      (string/replace-first "posts" "docs")))

;; get new names:
;; (map (comp (fn [x] (str x "/title.html")) post/->dst-path (fn [x] (.getParent x))) (post/get-all))

;; format dates:
;; (map (comp util/format-date util/path->date (fn [x] (.getPath x))) (post/get-all))
