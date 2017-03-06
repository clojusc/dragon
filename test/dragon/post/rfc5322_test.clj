(ns dragon.post.rfc5322-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [dragon.post.rfc5322 :as rfc5322]))

(deftest test-parse
  (let [data (rfc5322/parse (slurp (io/resource "sample-post.rfc5322")))]
    (is (= (:title data) "Pretty Cool Title"))
    (is (= (:subtitle data) "An Even Cooler Subtitle"))
    (is (= (count (:excerpt data)) 58))
    (is (= (:author data) "Yours V. Truly"))
    (is (= (:category data) "Fantastic Things"))
    (is (= (:tags data) "great, wonderful, stupendous, bees-knees, dogs-breakfast"))
    (is (= (:comment-link data) "http://github.com/forgotten-roads/blog/issue/42"))
    (is (= (:content-type data) "md"))
    (is (= (count (:body data)) 175))))
