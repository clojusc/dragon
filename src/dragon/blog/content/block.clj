(ns dragon.blog.content.block
  (:require [clojure.java.io :as io]
            [clojure.set :as sets]
            [clojure.string :as string]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag])
  (:import (java.io File)))

(defn legal-block-names
  ([]
    (legal-block-names #{}))
  ([other-names]
    (sets/union #{"pre-css"
                  "pre-head-scripts"
                  "post-head-scripts"
                  "head-postpends"
                  "post-post-scripts"}
                other-names)))

(defn legal-block-extensions
  ([]
    (legal-block-extensions #{}))
  ([other-extensions]
    (sets/union #{".selmer-block"}
                other-extensions)))

(def default-extension-separator ".")

(defn extension-regex
  [separator]
  (->> separator
       (str "\\")
       (re-pattern)))

(defn legal-block-name?
  ([block-name]
    (legal-block-name? (legal-block-names) block-name))
  ([legal-names block-name]
    (contains? legal-names block-name)))

(defn legal-block-extension?
  ([^File file]
    (legal-block-extension? (legal-block-extensions) file))
  ([legal-extensions ^File file]
    (->> legal-extensions
         (map #(string/ends-with? (.getCanonicalPath file) %))
         (remove false?)
         (empty?)
         (not))))

(defn get-block-name
  ([^File block-file]
    (get-block-name default-extension-separator block-file))
  ([extension-separator ^File block-file]
    (->> extension-separator
         extension-regex
         (string/split (.getName block-file))
         drop-last
         (string/join extension-separator))))

(defn legal-block-file?
  ([^File block-file]
    (legal-block-file? (legal-block-names) block-file))
  ([legal-names ^File block-file]
    (legal-block-file? legal-names (legal-block-extensions) block-file))
  ([legal-names legal-extensions ^File block-file]
    (legal-block-file?
     legal-names legal-extensions default-extension-separator block-file))
  ([legal-names legal-extensions extension-separator ^File block-file]
    (and (legal-block-name?
          legal-names
          (get-block-name extension-separator block-file))
         (legal-block-extension? legal-extensions block-file))))

(def illegal-block-file? (complement legal-block-file?))

(defn get-block-files
  ([parent-dir]
    (get-block-files (legal-block-names) parent-dir))
  ([legal-names parent-dir]
    (get-block-files legal-names (legal-block-extensions) parent-dir))
  ([legal-names legal-extensions parent-dir]
    (get-block-files
     legal-names legal-extensions default-extension-separator parent-dir))
  ([legal-names legal-extensions extension-separator parent-dir]
    (->> parent-dir
         (io/file)
         file-seq
         (remove (partial illegal-block-file?
                          legal-names legal-extensions extension-separator)))))

(defn get-block
  ([^File block-file]
    (get-block (legal-block-names) block-file))
  ([legal-names ^File block-file]
    (get-block legal-names (legal-block-extensions) block-file))
  ([legal-names legal-extensions block-file]
    (get-block
     legal-names legal-extensions default-extension-separator block-file))
  ([legal-names legal-extensions extension-separator block-file]
    (let [block-name (get-block-name extension-separator block-file)]
      (if (nil? block-name)
        []
        [(keyword block-name) (slurp block-file)]))))

(defn get-blocks
  ([post-data]
    (get-blocks (legal-block-names) post-data))
  ([legal-names ^File post-data]
    (get-blocks legal-names (legal-block-extensions) post-data))
  ([legal-names legal-extensions post-data]
    (get-blocks
     legal-names legal-extensions default-extension-separator post-data))
  ([legal-names legal-extensions extension-separator post-data]
    (->> post-data
         :src-dir
         (get-block-files legal-names legal-extensions extension-separator)
         (map (partial get-block legal-names legal-extensions extension-separator))
         (remove empty?)
         ((fn [x] (println "Got:" x) x))
         (into {}))))
