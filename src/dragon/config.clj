(ns dragon.config
  (:require [leiningen.core.project :as project]
            [dragon.util :as util]
            [taoensso.timbre :as log]))

(defn all
  []
  (project/read))

(defn dragon
  []
  (:dragon (all)))

(defn port
  []
  (:dev-port (dragon)))

(defn output-dir
  []
  (:output-dir (dragon)))

(defn cli
  []
  (:cli (dragon)))

(defn log-level
  []
  (:log-level (cli)))
