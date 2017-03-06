(ns dragon.util
  (:require [clojure.string :as string]
            [clojure.java.shell :as shell]
            [taoensso.timbre :as log]))

(defn get-build
  []
  (:out (shell/sh "git" "rev-parse" "--short" "HEAD")))

(defn now
  "Return the current time in two parts, ready to be used for creating
  blog post directories."
  []
  (let [now (new java.util.Date)
        ym-format (new java.text.SimpleDateFormat "YYYY-MM")
        dt-format (new java.text.SimpleDateFormat "dd-HHmmss")]
    {:ym (.format ym-format now)
     :dt (.format dt-format now)}))
