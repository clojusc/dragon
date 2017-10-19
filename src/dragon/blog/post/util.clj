(ns dragon.blog.post.util
  (:require [clojure.string :as string]
            [dragon.config.core :as config]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.util :as util]
            [markdown.core :as markdown]
            [taoensso.timbre :as log]))

(defn md->html
  [system md]
  (markdown/md-to-html-string
   md
   :inhibit-separator (config/template-skip-marker system)))

(defn convert-body!
  [system data content-type]
  (case content-type
    :md (update-in data [:body] (partial md->html system))))

(defn join-excerpt
  [system words number]
  (let [excerpt (string/join (config/word-joiner system)
                             (take number words))]
    (if (string/ends-with? excerpt (config/sentence-end system))
      (str excerpt (config/period-ellipsis system))
      (str excerpt (config/ellipsis system)))))

(defn copy-original-body
  [data]
  (assoc data :body-orig (:body data)))
