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
  [body tmpl-cfg]
  (markdown/md-to-html-string
   body
   :inhibit-separator (:skip-marker tmpl-cfg)))

(defn selmer->html
  [data body tmpl-cfg]
  (selmer/render body data))

(defn md+selmer->html
  [data body tmpl-cfg]
  (selmer->html
    data
    (md->html body tmpl-cfg)))

(defn selmer+md->html
  [data body tmpl-cfg]
  (md->html
    (selmer->html data body tmpl-cfg)
    tmpl-cfg))

(defn convert-body!
  [data tmpl-cfg]
  (case (:content-type data)
    :md
      (update-in data [:body] #(md->html % tmpl-cfg))
    :selmer
      (update-in data [:body] #(selmer->html data % tmpl-cfg))
    [:md :selmer]
      (update-in data [:body] #(md+selmer->html data % tmpl-cfg))
    [:selmer :md]
      (update-in data [:body] #(selmer+md->html data % tmpl-cfg))))

(defn join-excerpt
  ([system words number]
    (let [excerpt (string/join (config/word-joiner system)
                               (take number words))]
      (if (string/ends-with? excerpt (config/sentence-end system))
        (str excerpt (config/period-ellipsis system))
        (str excerpt (config/ellipsis system)))))
  ([system words number flag]
    (case flag
      :as-html (md->html (join-excerpt system words number)
                         (config/template-config system))
      (join-excerpt system words number))))

(defmulti get-content-element type)

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
