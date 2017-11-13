(ns dragon.selmer.tags.flickr-test
  (:require
    [cheshire.core :as json]
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [dragon.selmer.tags.flickr :as flickr]))

(defn get-sizes-payload
  []
  (-> "testing/payloads/flickr/get-sizes.json"
      (io/resource)
      (slurp)
      (flickr/extract-sizes)))

