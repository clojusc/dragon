(ns dragon.cli.new.stub
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojusc.twig :as logger]
            [dragon.config :as config]
            [dragon.util :as util]
            [taoensso.timbre :as log]))

(def stub-metadata
"{:title \"REQUIRED\"
 :subtitle \"\"
 :header-image \"\"
 :excerpt \"\"
 :author \"REQUIRED\"
 :twitter \"\"
 :category \"REQUIRED\"
 :tags []
 :comment-link \"\"
 :header-image \"\"}\n")

(def clj-content
"(defn metadata
  []
  {:title \"REQUIRED\"
   :subtitle \"\"
   :header-image \"\"
   :excerpt \"\"
   :author \"REQUIRED\"
   :twitter \"\"
   :category \"REQUIRED\"
   :tags []
   :comment-link \"\"
   :header-image \"\"})\n
(defn content
  []
  \"REQUIRED\")\n")

(def edn-content
"{:title \"REQUIRED\"
 :subtitle \"\"
 :header-image \"\"
 :excerpt \"\"
 :author \"REQUIRED\"
 :twitter \"\"
 :category \"REQUIRED\"
 :tags []
 :comment-link \"\"
 :header-image \"\"
 :content \"REQUIRED\"}\n")

;; Note that rfc5322-content uses the standard field names, when something
;; close exists. The following show how the RFC 5322 fields map to our metadata
;; fields:
;;
;; * Subject -> title
;; * From -> author (Fistname Lastname)
;; * Keywords -> tags (comma-separated)
;; * Comments -> comment-link
;;
;; The rest of the fields used are defined as "optional" in RFC 5322:
;;
;; * Subtitle
;; * Excerpt
;; * Category
;; * Content-Type

(def rfc5322-content
"Subject: REQUIRED (title)
Subtitle:
Header-Image:
Excerpt:
From: REQUIRED (author)
Twitter:
Category:
Keywords:
Comments:
Content-Type: md
Header-Image: img/

[content goes here]\n")

(defn stub-content
  [content-type]
  (case content-type
    :md ""
    :html ""
    :clj clj-content
    :edn edn-content
    :rfc5322 rfc5322-content))

(defn write-content
  [path content-type]
  (with-open [writer (io/writer (format
                                  "%s/content.%s" path (name content-type)))]
    (.write writer (stub-content content-type))))

(defn write-edn
  [path]
  (with-open [writer (io/writer (format "%s/meta.edn" path))]
    (.write writer stub-metadata)))

(defn write-files
  [{ym :ym dt :dt} content-type]
  (let [path (format "posts/%s/%s" ym dt)]
    (log/infof "Writing content and metadata files to %s ..." path)
    (io/make-parents (str path "/child"))
    (write-content path content-type)
    (when-not (or (= content-type :rfc5322)
                  (= content-type :edn)
                  (= content-type :clj))
      (write-edn path))))

(defn make-markdown-post
  [date]
  (write-files date :md))

(defn make-clojure-post
  [date]
  (write-files date :clj))

(defn make-edn-post
  [date]
  (write-files date :edn))

(defn make-html-post
  [date]
  (write-files date :html))

(defn make-rfc5322-post
  [date]
  (write-files date :rfc5322))
