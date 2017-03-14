(ns dragon.util
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.java.shell :as shell]
            [taoensso.timbre :as log]
            [trifl.fs :as fs]))

(def post-regex #"posts/(\d{4})-(\d{2})/(\d{2})-(\d{2})(\d{2})(\d{2})/.*")
(def timestamp-format ":year-:month-:day :hour::minute::second")
(def datestamp-format ":year-:month-:day")

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
  [date-map formater]
  (reduce
    (fn [acc [k v]]
      (string/replace acc (str k) v))
    formater
    date-map))

(defn format-timestamp
  [date-map]
  (format-date date-map timestamp-format))

(defn format-datestamp
  [date-map]
  (format-date date-map datestamp-format))

(defn sanitize-str
  [str]
  (-> str
      (string/replace #"\W+" "-")
      (string/lower-case)))

(defn month->name
  [month]
  (case month
    1 "January"
    2 "February"
    3 "March"
    4 "April"
    5 "May"
    6 "June"
    7 "July"
    8 "August"
    9 "September"
    10 "October"
    11 "November"
    12 "December"
    "01" "January"
    "02" "February"
    "03" "March"
    "04" "April"
    "05" "May"
    "06" "June"
    "07" "July"
    "08" "August"
    "09" "September"
    "10" "October"
    "11" "November"
    "12" "December"))

(defn month->short-name
  [month]
  (-> month
      (month->name)
      (subs 0 3)))

(defn dots->dashes
  [str]
  (string/replace str #"\." "-"))
