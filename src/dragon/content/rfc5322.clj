(ns dragon.content.rfc5322
  (:require [clojure.set :refer [rename-keys]]
            [clojure.string :as string]
            [instaparse.core :as instaparse]
            [rfc5322.core :as rfc]
            [rfc5322.dev :as rfc-dev]
            [taoensso.timbre :as log]))

(defn rfc5322-names->metadata-names
  "Given a field name, convert to a keyword column name."
  [^clojure.lang.Keyword field-name]
  (case field-name
    :subject :title
    :from :author
    :keywords :tags
    :comments :comment-link
    field-name))

(defn remap-keys
  "Update the keys in a map, converting them with the provided function."
  [mapper-fn ^clojure.lang.PersistentArrayMap data]
  (rename-keys
    data
    (->> data
         (keys)
         (map #(vector % (mapper-fn %)))
         (into {}))))

(defn parse
  "Parse the given message, converting the parsed tree into a Clojure map."
  [msg]
  (log/debug "Parsing message content ...")
  (->> msg
       (instaparse/parse (rfc/make-lite-parser))
       (rfc-dev/->map)
       (remap-keys rfc5322-names->metadata-names)))
