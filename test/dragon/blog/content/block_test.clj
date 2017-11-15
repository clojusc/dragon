(ns dragon.blog.content.block-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [dragon.blog.content.block :as block]))

(deftest legal-block-extension?
  (testing "allowed extensions"
    (is (block/legal-block-extension?
         (io/file
          (io/resource "testing/files/legal-block.selmer-block")))))
  (testing "disallowed extensions"
    (is (not (block/legal-block-extension?
              (io/file
               (io/resource "testing/files/illegal-block.bad-extension"))))))
  (testing "custom extensions"
    (is (not (block/legal-block-extension?
              (io/file
               (io/resource "testing/files/another-legal-block.custom-extension")))))
    (is (block/legal-block-extension?
         #{".custom-extension"}
         (io/file
          (io/resource "testing/files/another-legal-block.custom-extension"))))))

(deftest legal-block-name?
  (testing "allowed names"
    (doseq [block-name ["pre-css"
                        "pre-head-scripts"
                        "post-head-scripts"
                        "head-postpends"
                        "post-post-scripts"]]
      (is (block/legal-block-name? block-name))))
  (testing "disallowed names"
    (doseq [block-name ["some"
                        "other"
                        "block"
                        "names"]]
      (is (not (block/legal-block-name? block-name)))))
  (testing "custom names"
    (doseq [block-name ["custom"
                        "names"]]
      (is (block/legal-block-name?
           (block/legal-block-names #{"custom" "names"})
           block-name)))))

(deftest get-block-name
  (is (= "head-postpends"
         (block/get-block-name
          (io/file
           (io/resource "testing/files/head-postpends.selmer-block")))))
  (is (= "legal-block"
         (block/get-block-name
          (io/file
           (io/resource "testing/files/legal-block.selmer-block")))))
  (is (= "another-legal-block"
         (block/get-block-name
          (io/file
           (io/resource "testing/files/another-legal-block.custom-extension"))))))

(deftest legal-block-file?
  (testing "allowed extensions, allowed name"
    (is (block/legal-block-file?
         (io/file
          (io/resource "testing/files/head-postpends.selmer-block")))))
  (testing "allowed extensions, disallowed name"
    (is (not (block/legal-block-file?
              (io/file
               (io/resource "testing/files/legal-block.selmer-block"))))))
  (testing "disallowed extensions"
    (is (not (block/legal-block-file?
              (io/file
               (io/resource "testing/files/illegal-block.bad-extension"))))))
  (testing "custom extensions, disallowed name"
    (is (not (block/legal-block-file?
              (block/legal-block-names)
              (io/file
               (io/resource "testing/files/another-legal-block.custom-extension")))))
    (is (not (block/legal-block-file?
              (block/legal-block-names)
              (block/legal-block-extensions #{".custom-extension"})
              (io/file
               (io/resource "testing/files/another-legal-block.custom-extension"))))))
  (testing "custom extensions, allowed name"
    (is (block/legal-block-file?
         (block/legal-block-names #{"another-legal-block"})
         (block/legal-block-extensions #{".custom-extension"})
         (io/file
          (io/resource "testing/files/another-legal-block.custom-extension"))))))

(deftest illegal-block-file?
  (testing "allowed extensions, allowed name"
    (is (not (block/illegal-block-file?
              (io/file
               (io/resource "testing/files/head-postpends.selmer-block"))))))
  (testing "allowed extensions, disallowed name"
    (is (block/illegal-block-file?
         (io/file
          (io/resource "testing/files/legal-block.selmer-block")))))
  (testing "disallowed extensions"
    (is (block/illegal-block-file?
         (io/file
          (io/resource "testing/files/illegal-block.bad-extension")))))
  (testing "custom extensions, disallowed name"
    (is (block/illegal-block-file?
         (block/legal-block-names)
         (io/file
          (io/resource "testing/files/another-legal-block.custom-extension"))))
    (is (block/illegal-block-file?
         (block/legal-block-names)
         (block/legal-block-extensions #{".custom-extension"})
         (io/file
          (io/resource "testing/files/another-legal-block.custom-extension")))))
  (testing "custom extensions, allowed name"
    (is (not (block/illegal-block-file?
              (block/legal-block-names #{"another-legal-block"})
              (block/legal-block-extensions #{".custom-extension"})
              (io/file
               (io/resource "testing/files/another-legal-block.custom-extension")))))))

(deftest get-block-files
  (testing "with just default legal names"
    (is (= ["head-postpends.selmer-block"]
           (->> "testing/files"
                io/resource
                block/get-block-files
                (map #(.getName %))
                vec))))
  (testing "with added legal names"
    (is (= ["head-postpends.selmer-block"
            "legal-block.selmer-block"]
           (->> "testing/files"
                io/resource
                io/file
                (block/get-block-files
                 (block/legal-block-names #{"legal-block"}))
                (map #(.getName %))
                (sort)
                vec))))
  (testing "with added legal names and extensions"
    (is (= ["another-legal-block.custom-extension"
            "head-postpends.selmer-block"
            "legal-block.selmer-block"]
           (->> "testing/files"
                io/resource
                io/file
                (block/get-block-files
                 (block/legal-block-names #{"legal-block" "another-legal-block"})
                 (block/legal-block-extensions #{".custom-extension"}))
                (map #(.getName %))
                (sort)
                vec)))))

(deftest get-block
  (testing "with just default legal names"
    (is (= [:head-postpends "file data\n"]
           (block/get-block
            (io/file
               (io/resource "testing/files/head-postpends.selmer-block"))))))
  (testing "with added legal names"
    (is (= [:legal-block "more file data\n"]
           (block/get-block
            (block/legal-block-names #{"legal-block"})
            (io/file
               (io/resource "testing/files/legal-block.selmer-block"))))))
  (testing "with added legal names and extensions"
    (is (= [:another-legal-block "gobs and gobs of custom file data\n"]
           (block/get-block
            (block/legal-block-names #{"another-legal-block"})
            (block/legal-block-extensions #{".custom-extension"})
            (io/file
               (io/resource "testing/files/another-legal-block.custom-extension")))))))

(deftest get-blocks
  (testing "with just default legal names"
    (is (= {:head-postpends "file data\n"}
           (block/get-blocks
            {:src-dir (io/resource "testing/files")}))))
  (testing "with added legal names"
    (is (= {:head-postpends "file data\n"
            :legal-block "more file data\n"}
           (block/get-blocks
            (block/legal-block-names #{"legal-block"})
            {:src-dir (io/resource "testing/files")}))))
  (testing "with added legal names and extensions"
    (is (= {:head-postpends "file data\n"
            :another-legal-block "gobs and gobs of custom file data\n"}
           (block/get-blocks
            (block/legal-block-names #{"another-legal-block"})
            (block/legal-block-extensions #{".custom-extension"})
            {:src-dir (io/resource "testing/files")})))))
