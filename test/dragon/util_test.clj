(ns dragon.util-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [dragon.util :as util]))

(deftest path->date
  (is (= (util/path->date "posts/2017-01/07-050909/content.rfc5322")
         {:year "2017" :month "01" :day "07"
          :hour "05" :minute "09" :second "09"})))

(deftest format-date
  (testing "datestamp"
    (is (= (util/format-date {:year "2017" :month "01" :day "07"
                              :hour "05" :minute "09" :second "09"}
                             util/datestamp-format)
           "2017-01-07")))
  (testing "timestamp"
    (is (= (util/format-date {:year "2017" :month "01" :day "07"
                              :hour "05" :minute "09" :second "09"}
                             util/timestamp-format)
           "2017-01-07 05:09:09"))))

(deftest month->name
  (is (= (util/month->name "12") "December"))
  (is (= (util/month->name "01") "January")))

(deftest month->short-name
  (is (= (util/month->short-name "12") "Dec"))
  (is (= (util/month->short-name "01") "Jan")))

(deftest deep-merge
  (is (= {:a 2}
         (util/deep-merge {:a 1} {:a 2})))
  (is (= {:a 1 :b 2}
         (util/deep-merge {:a 1} {:b 2})))
  (is (= {:a {:b 2}}
         (util/deep-merge {:a {:b 1}} {:a {:b 2}})))
  (is (= {:a {:b 1 :c 2}}
         (util/deep-merge {:a {:b 1}} {:a {:c 2}})))
  (is (= {:a {:b {:c {:d {:e {:f 1 :g 2}}}}}}
         (util/deep-merge {:a {:b {:c {:d {:e {:f 1}}}}}}
                          {:a {:b {:c {:d {:e {:g 2}}}}}}))))

