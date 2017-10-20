(ns dragon.config.defaults
  (:require [dragon.config.datomic :as datomic]
            [dragon.config.redis :as redis]))

(def config
  {:port 5097
   :domain "dragon.github.io"
   :name "Dragon Blog Generator"
   :description "A fire-breathing blog generator"
   :output-dir "docs"
   :output-file-tmpl "%s.html"
   :base-path "/blog"
   :posts-path "/blog/archives"
   :posts-path-src "./posts"
   :feed-count 20
   :link-tmpl "<a href=\"%s\">%s</a>"
   :templating {
     :skip-marker "%%%"}
   :parsing {
     :word-separator #"\s"
     :word-joiner " "
     :paragraph-separator #"\n\n"
     :tag-separator #",\s?"
     :sentence-end "."
     :ellipsis " ..."
     :period-ellipsis ".."}
   :repl {
     :log-level :info
     :log-nss '[dragon]}
   :app {
     :log-level :warn
     :log-nss '[dragon]}
   :cli {
     :log-level :error
     :log-nss '[dragon]}
   :processor {
     :constructor :default}
   :workflow {
     ;:type :transducer
     :type :iterator
     ;:storage :db
     :storage :memory}
   :db {
     :type :redis
     :redis redis/config
     :datomic datomic/config}})
