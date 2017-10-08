(ns dragon.util
  (:require [clojure.string :as string]
            [clojure.java.shell :as shell]
            [clojusc.twig :refer [pprint]]
            [selmer.parser :as selmer]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [boolean]))

(declare merge-val)

(def char-regex #"^a*")
(def post-regex #"(\./)?posts/(\d{4})-(\d{2})/(\d{2})-(\d{2})(\d{2})(\d{2})/.*")
(def timestamp-format ":year-:month-:day :hour::minute::second")
(def datestamp-format ":year-:month-:day")

(defn get-build
  []
  (:out (shell/sh "git" "rev-parse" "--short" "HEAD")))

(defn post-now
  "Return the current time in two parts, ready to be used for creating
  blog post directories."
  []
  (let [now (new java.util.Date)
        ym-format (new java.text.SimpleDateFormat "YYYY-MM")
        dt-format (new java.text.SimpleDateFormat "dd-HHmmss")]
    {:ym (.format ym-format now)
     :dt (.format dt-format now)}))

(defn path->date
  [dir]
  (dissoc
    (->> dir
         (re-matches post-regex)
         (zipmap [:all :dir-prefix :year :month :day :hour :minute :second]))
    :all :dir-prefix))

(defn datetime-now
  []
  (let [{:keys [ym dt]} (post-now)]
    (path->date (format "posts/%s/%s/" ym dt))))

(defn now
  ([]
    (now :post-map))
  ([date-format]
    (case date-format
      :datetime-map (datetime-now)
      :post-map (post-now))))

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
  [data-str]
  (-> data-str
      (string/replace #"\W+" "-")
      (string/lower-case)))

(defn sanitize-post-path
  [path-str]
  (string/replace path-str #"^\./" ""))

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

(defn count-chars
  [str]
  (log/trace "Counting chars ...")
  (count (re-seq #"." str)))

(defn count-words
  [str]
  (log/trace "Counting words ...")
  (count (re-seq #"[^\s]+" str)))

(defn count-lines
  [str]
  (log/trace "Counting lines ...")
  (as-> str data
        (string/split data #"\n")
        (remove empty? data)
        (count data)))

(defn deep-merge
  ""
  [data1 data2]
  (merge-with merge-val data1 data2))

(defmulti merge-val
  (fn [a b]
    (type a)))

(defmethod merge-val clojure.lang.PersistentVector
  [a b]
  (concat a b))

(defmethod merge-val clojure.lang.PersistentArrayMap
  [a b]
  (deep-merge a b))

(defmethod merge-val :default
  [a b]
  b)

(defn render
  [template data]
  (log/tracef "Rendering template %s with data %s" template (pprint data))
  (selmer/render-file
    template
    data))

(defn component->system
  ""
  [system-or-component]
  (if (contains? system-or-component :event)
    system-or-component
    {:event system-or-component}))

(defn boolean
  "A filter that returns `true` if the post should be published on the front
  page."
  [post key]
  (if (= (string/lower-case (or (key post) "")) "false")
    false
    true))

(defn headline?
  "A filter that returns `true` if the post should be published on the front
  page."
  [post]
  (boolean post :headlines?))

(defn public?
  "A filter that returns `true` if the post should be published."
  [post]
  (boolean post :public?))
