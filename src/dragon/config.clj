(ns dragon.config
  (:require [dragon.util :as util]
            [leiningen.core.project :as project]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [name read]))

(def defaults
  {:dev-port 5097
   :domain "dragon.github.io"
   :name "Dragon Blog Generator"
   :description "A fire-breathing blog generator"
   :output-dir "docs"
   :base-path "/blog"
   :posts-path "/blog/archives"
   :posts-path-src "./posts"
   :feed-count 20
   :cli {
     :log-level :info
     :log-nss '[dragon]}})

(defn build
  ""
  []
  (util/deep-merge
   defaults
   (:dragon (project/read))))

(defn domain
  [system]
  (get-in system [:config :dragon :domain]))

(defn domain-urn
  [system]
  (->> system
       (domain)
       (util/dots->dashes)
       (format "urn:%s")))

(defn name
  [system]
  (get-in system [:config :dragon :name]))

(defn description
  [system]
  (get-in system [:config :dragon :description]))

(defn port
  [system]
  (get-in system [:config :dragon :port]))

(defn output-dir
  [system]
  (get-in system [:config :dragon :output-dir]))

(defn base-path
  [system]
  (get-in system [:config :dragon :base-path]))

(defn posts-path
  [system]
  (get-in system [:config :dragon :posts-path]))

(defn posts-path-src
  [system]
  (get-in system [:config :dragon :posts-path-src]))

(defn feed-count
  [system]
  (get-in system [:config :dragon :feed-count]))

(defn cli
  [system]
  (get-in system [:config :dragon :cli]))

(defn log-level
  [system]
  (get-in system [:config :dragon :cli :log-level]))

(defn log-nss
  [system]
  (get-in system [:config :dragon :cli :log-nss]))
