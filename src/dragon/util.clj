(ns dragon.util
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.java.shell :as shell]
            [taoensso.timbre :as log]
            [trifl.fs :as fs]))

(def post-regex #"posts/(\d{4})-(\d{2})/(\d{2})-(\d{2})(\d{2})(\d{2})/.*")
(def date-format ":year-:month-:day :hour::minute::second")

(defn get-build
  []
  (:out (shell/sh "git" "rev-parse" "--short" "HEAD")))

(defn now
  "Return the current time in two parts, ready to be used for creating
  blog post directories."
  []
  (let [now (new java.util.Date)
        ym-format (new java.text.SimpleDateFormat "YYYY-MM")
        dt-format (new java.text.SimpleDateFormat "dd-HHmmss")]
    {:ym (.format ym-format now)
     :dt (.format dt-format now)}))

(defn get-files
  [dir]
  (->> dir
       (io/file)
       (file-seq)
       (filter fs/file?)))

(defn path->date
  [dir]
  (dissoc
    (->> dir
        (re-matches post-regex)
        (zipmap [:all :year :month :day :hour :minute :second]))
    :all))

(defn format-date
  [date-map]
  (reduce
    (fn [acc [k v]]
      (string/replace acc (str k) v))
    date-format
    date-map))

(defn sanitize-str
  [str]
  (-> str
      (string/replace #"\W+" "-")
      (string/lower-case)))
