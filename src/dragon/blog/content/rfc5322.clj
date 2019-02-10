  (ns dragon.blog.content.rfc5322
  (:require
    [clojure.set :refer [rename-keys]]
    [clojure.string :as string]
    [clojure.walk :as walk]
    [instaparse.core :as instaparse]
    [rfc5322.core :as rfc5322]
    [taoensso.timbre :as log]))

(def rfc5322-names->metadata-names
  "Used to convert to internet message header fields to internally-used
  metadata fields."
  {:subject :title
   :from :author
   :keywords :tags
   :comments :comment-link})

(defn parse
  "Parse the given message, converting the parsed tree into a Clojure map."
  [msg]
  (log/debug "Parsing message content ...")
  (->> (rfc5322/convert msg :lite :utf8)
       (walk/postwalk-replace rfc5322-names->metadata-names)
       (into {})))
