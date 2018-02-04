(ns dragon.config.core
  (:require
    [clojure.string :as string]
    [dragon.components.core :as components]
    [dragon.config.defaults :as default]
    [dragon.util :as util]
    [leiningen.core.project :as project]
    [taoensso.timbre :as log]
    [trifl.fs :as fs])
  (:refer-clojure :exclude [name read]))

(defn read-home-file
  [file-path]
  (-> file-path
      fs/expand-home
      slurp
      string/trim))

(defn build
  ""
  []
  (let [proj (project/read)]
    (util/deep-merge
     default/config
     (util/deep-merge
      (get-in proj [:profiles :dragon])
      (:dragon (project/read-profiles proj))))))
