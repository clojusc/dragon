(ns dragon.config
  (:require [leiningen.core.project :as project]
            [dragon.util :as util]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [name]))

(defn all
  []
  (project/read))

(defn dragon
  []
  (:dragon (all)))

(defn domain
  []
  (:domain (dragon)))

(defn domain-urn
  []
  (format "urn:%s" (util/dots->dashes (domain))))

(defn name
  []
  (:name (dragon)))

(defn description
  []
  (:description (dragon)))

(defn port
  []
  (:dev-port (dragon)))

(defn output-dir
  []
  (:output-dir (dragon)))

(defn base-path
  []
  (:base-path (dragon)))

(defn posts-path
  []
  (:posts-path (dragon)))

(defn posts-path-src
  []
  (:posts-path-src (dragon)))

(defn feed-count
  []
  (:feed-count (dragon)))

(defn cli
  []
  (:cli (dragon)))

(defn log-level
  []
  (:log-level (cli)))

(defn log-ns
  []
  (:log-ns (cli)))
