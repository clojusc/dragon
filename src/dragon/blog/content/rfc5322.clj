(ns dragon.blog.content.rfc5322
  (:require [clojure.set :refer [rename-keys]]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [instaparse.core :as instaparse]
            [rfc5322.core :as rfc]
            [rfc5322.dev :as rfc-dev]
            [taoensso.timbre :as log]))

(def rfc5322-names->metadata-names
  "Used to convert to internet message header fields to internally-used
  metadata fields."
  {:subject :title
   :from :author
   :keywords :tags
   :comments :comment-link})

; (defn remap-keys
;   "Update the keys in a map, converting them with the provided function."
;   [mapper-fn ^clojure.lang.PersistentArrayMap data]
;   (rename-keys
;     data
;     (->> data
;          (keys)
;          (map #(vector % (mapper-fn %)))
;          (into {}))))

(defn parse
  "Parse the given message, converting the parsed tree into a Clojure map."
  [system msg]
  (log/debug "Parsing message content ...")
  (event/publish system tag/parse-file-pre)
  (->> msg
       (instaparse/parse (rfc/make-lite-parser))
       (rfc-dev/->map)
       (walk/postwalk-replace rfc5322-names->metadata-names)
       (into {})
       (event/publish->> system tag/parse-file-post)))
