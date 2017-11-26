(ns dragon.selmer.tags.demo
  (:require [clojure.string :as string]))

(defn foo-tag
  [args context-map]
  (str "foo " (first args)))
