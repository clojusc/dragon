(ns dragon.cli.new.post
  (:require
    [clojure.pprint :refer [pprint]]
    [dragon.cli.new.stub :as stub]
    [dragon.util :as util]
    [taoensso.timbre :as log]
    [trifl.docs :as docs]))

(defn run
  "
  Usage:
  ```
    dragon new post SUBCOMMAND [help]
  ```

  A SUBCOMMAND is required.

  Subcommands:
  ```
    md         Create a new post stub in Markdown format
    clj        Create a new post stub in Clojure format
    edn        Create a new post stub in EDN format
    html       Create a new post stub in HTML format
    rfc5322    Create a new post stub in a format based on email messages;
                 in this case, no metadata file is created (message headers
                 are used instead) and the `content-type` field is used to
                 indicate the content type of the body of the message (i.e.,
                 blog post)
  ```"
  [system [cmd & args]]
  (log/debug "Got cmd:" cmd)
  (log/debug "Got args:" args)
  (let [passed-date (first args)
        date (or passed-date (util/now))]
    (case cmd
      :md (stub/make-markdown-post date)
      :clj (stub/make-clojure-post date)
      :edn (stub/make-edn-post date)
      :html (stub/make-html-post date)
      :rfc5322 (stub/make-rfc5322-post date)
      (docs/print-docstring #'run))))
