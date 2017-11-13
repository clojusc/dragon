(ns dragon.blog.post.util
  (:require [clojure.string :as string]
            [dragon.config.core :as config]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.util :as util]
            [markdown.core :as markdown]
            [selmer.parser :as selmer]
            [taoensso.timbre :as log]))

(defn md->html
  [system md]
  (markdown/md-to-html-string
   md
   :inhibit-separator (config/template-skip-marker system)))

(defn selmer->html
  [system data content]
  (selmer/render content (assoc data :system system)))

(defn md+selmer->html
  [system data content]
  (->> content
       (md->html system)
       (selmer->html system data)))

(defn selmer+md->html
  [system data content]
  (->> content
       (selmer->html system data)
       (md->html system)))

(defn convert-body!
  [system data content-type]
  (case content-type
    :md
      (update-in data [:body] (partial md->html system))
    :selmer
      (update-in data [:body] (partial selmer->html system data))
    [:md :selmer]
      (update-in data [:body] (partial md+selmer->html system data))
    [:selmer :md]
      (update-in data [:body] (partial selmer+md->html system data))))

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
