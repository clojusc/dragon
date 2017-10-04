(ns dragon.config
  (:require [dragon.components.core :as components]
            [dragon.util :as util]
            [leiningen.core.project :as project]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [name read]))

(def defaults
  {:dev-port 5097
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
     :log-nss '[dragon]}})

(defn build
  ""
  []
  (util/deep-merge
   defaults
   (:dragon (project/read))))

(defn domain
  [system]
  (components/get-config system :domain))

(defn domain-urn
  [system]
  (->> system
       (domain)
       (util/dots->dashes)
       (format "urn:%s")))

(defn name
  [system]
  (components/get-config system :name))

(defn description
  [system]
  (components/get-config system :description))

(defn port
  [system]
  (components/get-config system :port))

(defn output-dir
  [system]
  (components/get-config system :output-dir))

(defn output-file-tmpl
  [system]
  (components/get-config system :output-file-tmpl))

(defn base-path
  [system]
  (components/get-config system :base-path))

(defn posts-path
  [system]
  (components/get-config system :posts-path))

(defn posts-path-src
  [system]
  (components/get-config system :posts-path-src))

(defn feed-count
  [system]
  (components/get-config system :feed-count))

(defn link-tmpl
  [system]
  (components/get-config system :link-tmpl))

(defn cli
  [system]
  (components/get-config system :cli))

(defn log-level
  [system]
  (components/get-config system :cli :log-level))

(defn log-nss
  [system]
  (components/get-config system :cli :log-nss))

(defn repl
  [system]
  (components/get-config system :repl))

(defn repl-log-level
  [system]
  (components/get-config system :repl :log-level))

(defn repl-log-nss
  [system]
  (components/get-config system :repl :log-nss))

(defn app
  [system]
  (components/get-config system :app))

(defn app-log-level
  [system]
  (components/get-config system :app :log-level))

(defn app-log-nss
  [system]
  (components/get-config system :app :log-nss))

(defn templating
  [system]
  (components/get-config system :templating))

(defn template-skip-marker
  [system]
  (components/get-config system :templating :skip-marker))

(defn parsing
  [system]
  (components/get-config system :parsing))

(defn word-separator
  [system]
  (components/get-config system :parsing :word-separator))

(defn word-joiner
  [system]
  (components/get-config system :parsing :word-joiner))

(defn paragraph-separator
  [system]
  (components/get-config system :parsing :paragraph-separator))

(defn tag-separator
  [system]
  (components/get-config system :parsing :tag-separator))

(defn sentence-end
  [system]
  (components/get-config system :parsing :sentence-end))

(defn ellipsis
  [system]
  (components/get-config system :parsing :ellipsis))

(defn period-ellipsis
  [system]
  (components/get-config system :parsing :period-ellipsis))
