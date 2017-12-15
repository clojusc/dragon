(ns dragon.config.defaults
  (:require [dragon.config.datomic :as datomic]
            [dragon.config.redis :as redis]))

(def config
  {:name "Dragon Blog Generator"
   :description "A fire-breathing blog generator"
   :domain "dragon.github.io"
   :port 5097
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
   :blocks {
     :enabled #{}}
   :robots {
     :allow #{"/blog"}
     :disallow #{}}
   :repl {
     :log-level :info
     :log-nss '[dragon]}
   :app {
     :log-level :warn
     :log-nss '[dragon]}
   :cli {
     :log-level :error
     :log-nss '[dragon]}
   :apis {
     :flickr {
       :access "~/.flickr/access.key"}
     :twitter {
       :app-consumer {
         :key "~/.twitter/app-consumer.key"
         :secret "~/.twitter/app-consumer.secret"}
       :user-access {
         :token "~/.twitter/user-access.token"
         :secret "~/.twitter/user-access.secret"}}}
   :processor {
     :constructor :default}
   :workflow {
     ;:type :transducer
     :type :iterator
     ;:storage :db
     :storage :memory}
   :db {
     :type :redis-docker
     :redis-docker redis/config-docker
     :redis-native redis/config-native
     :datomic datomic/config}})
