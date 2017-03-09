(ns dragon.util-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [dragon.util :as util]))

(deftest path->date
  (is (= (util/path->date "posts/2017-01/07-050909/content.rfc5322"))))
