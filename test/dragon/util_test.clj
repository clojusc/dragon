(ns dragon.util-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [dragon.util :as util]))

(deftest path->date
  (is (= (util/path->date "posts/2017-01/07-050909/content.rfc5322")
         {:year "2017" :month "01" :day "07"
          :hour "05" :minute "09" :second "09"})))

(deftest format-date
  (is (= (util/format-date {:year "2017" :month "01" :day "07"
                            :hour "05" :minute "09" :second "09"})
         "2017-01-07 05:09:09")))
