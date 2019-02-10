(ns dragon.repl
  "Dragon development namespace.

  This namespace is particularly useful when doing active development on dependent
  blog applications."
  (:require
    [cheshire.core :as json]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.java.shell :as shell]
    [clojure.pprint :refer [pprint print-table]]
    [clojure.reflect :refer [reflect]]
    [clojure.string :as string]
    [clojure.tools.namespace.repl :as repl]
    [clojure.walk :refer [macroexpand-all]]
    [clojusc.system-manager.core :refer :all]
    [clojusc.twig :as logger]
    [com.stuartsierra.component :as component]
    [dragon.blog.content.block :as block]
    [dragon.blog.content.rfc5322 :as rfc5322]
    [dragon.blog.core :as blog]
    [dragon.blog.generator :as generator]
    [dragon.blog.post.core :as post]
    [dragon.blog.post.util :as post-util]
    [dragon.cli.core :as cli]
    [dragon.cli.show.posts :as cli-show-posts]
    [dragon.components.core :as components]
    [dragon.components.db :as db-component]
    [dragon.config :as config]
    [dragon.core]
    [dragon.data.sources.core :as data-source]
    [dragon.data.sources.impl.redis :as redis-db]
    [dragon.main :as main]
    [dragon.selmer.tags.flickr :as flickr]
    [dragon.util :as util]
    [markdown.core :as md]
    [selmer.parser :as selmer]
    [taoensso.carmine :as car :refer [wcar]]
    [taoensso.timbre :as log]
    [trifl.core :refer [->int]]
    [trifl.fs :as fs]
    [trifl.java :refer [show-methods]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Initial Setup & Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic generator-ns "")

(defn set-generator-ns
  [an-ns]
  (alter-var-root #'generator-ns (constantly an-ns)))

(def setup-options {
  :init 'dragon.components.core/init
  :after-refresh 'dragon.repl/init-and-startup
  :throw-errors false})

(defn init
  "This is used to set the options and any other global data.

  This is defined in a function for re-use. For instance, when a REPL is
  reloaded, the options will be lost and need to be re-applied."
  []
  (selmer/cache-off!)
  (logger/set-level! '[dragon] :debug)
  (setup-manager setup-options))

(defn init-and-startup
  "This is used as the 'after-refresh' function by the REPL tools library.
  Not only do the options (and other global operations) need to be re-applied,
  the system also needs to be started up, once these options have be set up."
  []
  (init)
  (startup))

;; It is not always desired that a system be started up upon REPL loading.
;; Thus, we set the options and perform any global operations with init,
;; and let the user determine when then want to bring up (a potentially
;; computationally intensive) system.
(init)

(defn banner
  []
  (println (slurp (io/resource "text/banner.txt")))
  :ok)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Initial Setup & Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn resolve-by-name
  [an-ns a-fun]
  (println "DEBUG: got args: " an-ns a-fun)
  (resolve (symbol (str an-ns "/" a-fun))))

(defn call-by-name
  [an-ns a-fun & args]
  (println "DEBUG: got args: " an-ns a-fun args)
  (apply (resolve-by-name an-ns a-fun) args))

(defn redis
  [& args]
  (apply redis-db/cmd (concat [system] args)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn show-lines-with-error
  "Process posts and show the lines of text that threw exceptions."
  ([]
    (show-lines-with-error #'system))
  ([system]
    (let [processor (post/new-processor system)]
      (->> processor
           (blog/get-posts)
           (map #(->> %
                     (post/get-data processor)
                     :text))
           (pprint)))))

(defn show-posts
  []
  (cli-show-posts/run system))

(defn generate
  []
  (call-by-name generator-ns "generate" system))

(defn force-regenerate
  []
  (call-by-name generator-ns "force-regenerate" system))

(comment
  (dragon.blog.workflow.core/new-workflow system [:iterator :db])
  (-> system
      dragon.blog.core/get-posts
  )
