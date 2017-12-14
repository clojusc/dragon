(ns dragon.data.page
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [dragon.blog.content.block :as block]
    [dragon.blog.core :as blog]
    [dragon.blog.tags :as blog-tags]
    [dragon.config.core :as config]
    [markdown.core :as markdown]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Constants & Helper Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn resource-content
  [resource-filename]
  (->> resource-filename
       (io/resource)
       (slurp)))

(defn markdown-content
  [md-filename]
  (->> md-filename
       (str "markdown/")
       (resource-content)
       (markdown/md-to-html-string)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Base Data Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn posts-stats
  [posts]
  {:posts (count posts)
   :authors (->> posts
                 (map :author)
                 set
                 count)
   :lines (->> posts
               (map :line-count)
               (reduce +))
   :words (->> posts
               (map :word-count)
               (reduce +))
   :chars (->> posts
               (map :char-count)
               (reduce +))})

(defn base
  ([]
    (base {}))
  ([opts]
    {:page-data {
       :base-path "/blog"
       :site-title (:site-title opts)
       :site-description (:site-description opts)
       :index "index"
       :about "about"
       :community "community"
       :archives "archives"
       :categories "categories"
       :tags "tags"
       :authors "authors"
       :active (:category-key opts)}}))

(defn default-content-opts
  []
  {:title ""
   :subtitle ""
   :category-key ""
   :base-data-fn base
   :content-filename ""
   :content-fn (constantly nil)})

(defn default-data-content-opts
  []
  (assoc (default-content-opts) :content-fn identity))

(defn default-resource-content-opts
  []
  (assoc (default-content-opts) :content-fn resource-content))

(defn default-markdown-content-opts
  []
  (assoc (default-content-opts) :content-fn markdown-content))

(defn common
  [posts additional-opts]
  (let [opts (merge (default-content-opts) additional-opts)
        base-data-fn (:base-data-fn opts)
        content-fn (:content-fn opts)
        content-filename (:content-filename opts)]
    (-> opts
        (base-data-fn)
        (assoc-in [:page-data :active] (name (:category-key opts)))
        (dissoc :title :subtitle)
        (assoc :posts-data posts
               :posts-stats (posts-stats posts)
               :content {
                 :title (:title opts)
                 :subtitle (:subtitle opts)
                 :body (content-fn content-filename)}))))
