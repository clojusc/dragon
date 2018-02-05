(ns dragon.blog.post.util
  (:require
    [clojure.string :as string]
    [dragon.components.config :as config]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [dragon.util :as util]
    [markdown.core :as markdown]
    [net.cgrand.enlive-html :as en]
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
  ([system words number]
    (let [excerpt (string/join (config/word-joiner system)
                               (take number words))]
      (if (string/ends-with? excerpt (config/sentence-end system))
        (str excerpt (config/period-ellipsis system))
        (str excerpt (config/ellipsis system)))))
  ([system words number flag]
    (case flag
      :as-html (md->html system (join-excerpt system words number))
      (join-excerpt system words number))))

(defmulti get-content-element
  type)

(defmethod get-content-element java.lang.String
  [element]
  element)

(defmethod get-content-element clojure.lang.PersistentStructMap
  [element]
  (get-content-element (:content element)))

(defmethod get-content-element :default
  [element]
  (mapcat get-content-element element))

(defn- parse-html-str
  [html-str]
  (->> html-str
       (new java.io.StringReader)
       (en/html-resource)))

(defn normalize-whitespace
  [a-str]
  (-> a-str
      (string/replace #"\\" "")
      (string/replace #"\s{2,}" " ")
      string/trim))

(defn scrub-html
  [html-content]
  (->> html-content
       parse-html-str
       get-content-element
       (apply str)
       normalize-whitespace))

(defn copy-original-body
  [data]
  (assoc data :body-orig (:body data)))
