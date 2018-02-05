(ns dragon.cli.show.posts
  (:require
    [clojure.string :as string]
    [clojusc.twig :as logger]
    [dragon.data.sources.core :as data]))

(defn- clean-post-key
  [post-key]
  (nth
   (re-matches #"(^\./)(.*)(:.*$)" post-key)
   2))

(defn get-posts
  [system]
  (let [querier (get-in system [:db :querier])]
    (->> querier
         data/get-post-keys
         (map #(data/get-raw querier %)))))

(defn format-post
  [post-data]
  (format "%s - %s%s"
            (:src-file post-data)
            (:title post-data)
            (if-let [subtitle (:subtitle post-data)]
              (str ": " subtitle)
              "")))

(defn print-posts
  [system]
  (doseq [post (get-posts system)]
    (println (format-post post)))
  :ok)

(defn run
  [system]
  (logger/set-level! '[mx.roads dragon] :fatal)
  (Thread/sleep 1000)
  (print-posts system))
