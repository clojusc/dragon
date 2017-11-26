(ns dragon.selmer.tags.util
  (:require
    [clojure.string :as string]
    [markdown.core :as markdown]))

;; Once we generate HTML that we don't want to be re-interpreted by Selmer in
;; any future/additional passes, we need to escape it.
(def escaped-str-skip-marker "%%%")
(def escaped-format-skip-marker "%%%%%%")

(defn ->int
  [arg]
  (if (integer? arg)
    arg
    (Integer/parseInt arg)))

(defn str-kwd->kwd
  [[k v]]
  [(keyword (string/join str (rest k))) (string/replace v "\"" "")])

(defn args->map
  [args]
  (->> args
       (partition 2)
       (mapv str-kwd->kwd)
       (into {})))

(defn stripped-str
  [text]
  (->> (string/split text #"\n")
       (map string/trim)
       (string/join "\n")))

(defn stripped-md->html
  [md]
  (->> md
       (stripped-str)
       (markdown/md-to-html-string)))
