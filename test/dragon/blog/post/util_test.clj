(ns dragon.blog.post.util-test
  (:require
    [clojure.test :refer :all]
    [dragon.blog.post.util :as post-util]))

(deftest scrub-html
	(testing "Simple html"
		(is (= "My content stuff."
			     (post-util/scrub-html "<p>My content stuff</p>.")))
		(is (= "My content stuff."
			     (post-util/scrub-html "My <p>content</p> stuff.")))
		(is (= "My content stuff."
			     (post-util/scrub-html "My <p>content stuff</p>."))))
	(testing "Nested html"
		(is (= "My content stuff."
			     (post-util/scrub-html
			     	 "<div><p>My content <i>stuff</i></p></div>."))))
	(testing "Deeply nested html"
		(is (= "My content stuff."
			     (post-util/scrub-html
			     	 "<div><p>My<ul> \\
			     	              <li>content</li> \\
			     	              <li><i>stuff</i>.</li> \\
			     	            </ul> \\
			     	       </p> \\
			     	  </div>")))))