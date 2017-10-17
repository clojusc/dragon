(ns dragon.config
  (:require ;[datomic.client :as datomic]
            [dragon.components.core :as components]
            [dragon.util :as util]
            [leiningen.core.project :as project]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [name read]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Redis   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private redis-config
  {:container-id-file "/tmp/redis-dragon-docker-id"
   :image-name "redis:4.0.2-alpine"
   :host "localhost"
   :port "6379"
   :host-data-dir "data"
   :guest-data-dir "/data"})

(def ^:private redis-start
  {:home (System/getProperty "user.dir")
   :executable "docker"
   :command "redis-server"})

(def ^:private redis-start-args
  [(:executable redis-start)
   "run"
   "-d"
   "-v" (format "%s/%s:%s" (:home redis-start)
                           (:host-data-dir redis-config)
                           (:guest-data-dir redis-config))
   "--cidfile" (:container-id-file redis-config)
   "-p" (format "%s:%s" (:port redis-config) (:port redis-config))
   (:image-name redis-config)
   (:command redis-start)
   "--appendonly" "yes"])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Datomic   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private datomic-config
  {:version "0.9.5561.62"
   :host "localhost"
   :port "8998"
   :access-key "dragon"
   :secret "dragon"
   :db-name "dragon"})

(def ^:private datomic-start
  {:delay 5000
   :retry-delay 500
   :retry-timeout 10000
   :home (str "/opt/datomic/" (:version datomic-config))
   :executable "bin/run"
   :entry-point "datomic.peer-server"
   :host (:host datomic-config)
   :port (:port datomic-config)
   :db (format "%s,datomic:mem://%s" (:db-name datomic-config)
                                    (:db-name datomic-config))
   :auth (format "%s,%s" (:access-key datomic-config)
                           (:secret datomic-config))})

(def ^:private datomic-start-args
  [(:executable datomic-start)
   "-m" (:entry-point datomic-start)
   "-h" (:host datomic-start)
   "-p" (:port datomic-start)
   "-d" (:db datomic-start)
   "-a" (:auth datomic-start)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   All   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def defaults
  {:port 5097
   :domain "dragon.github.io"
   :name "Dragon Blog Generator"
   :description "A fire-breathing blog generator"
   :output-dir "docs"
   :output-file-tmpl "%s.html"
   :base-path "/blog"
   :posts-path "/blog/archives"
   :posts-path-src "./posts"
   :feed-count 20
   :link-tmpl "<a href=\"%s\">%s</a>"
   :templating {
     :skip-marker "%%%"}
   :parsing {
     :word-separator #"\s"
     :word-joiner " "
     :paragraph-separator #"\n\n"
     :tag-separator #",\s?"
     :sentence-end "."
     :ellipsis " ..."
     :period-ellipsis ".."}
   :repl {
     :log-level :info
     :log-nss '[dragon]}
   :app {
     :log-level :warn
     :log-nss '[dragon]}
   :cli {
     :log-level :error
     :log-nss '[dragon]}
   :db {
     :type :redis
     :redis {
       :start (assoc redis-start :args redis-start-args)
       :container-id-file (:container-id-file redis-config)
       :conn {
         :pool {}
         :spec {
           :host (:host redis-config)
           :port (Integer/parseInt (:port redis-config))}}}
     :datomic {
       :version (:version datomic-config)
       :start (assoc datomic-start :args datomic-start-args)
       :conn {
         ;:account-id datomic/PRO_ACCOUNT
         ;:region datomic/PRO_REGION
         :service "peer-server"
         :endpoint (format "%s:%s" (:host datomic-config)
                                   (:port datomic-config))
         :db-name (:db-name datomic-config)
         :access-key (:access-key datomic-config)
         :secret (:secret datomic-config)}}}})

(defn build
  ""
  []
  (util/deep-merge
   defaults
   (:dragon (project/read))))

(defn domain
  [system]
  (components/get-config system :domain))

(defn domain-urn
  [system]
  (->> system
       (domain)
       (util/dots->dashes)
       (format "urn:%s")))

(defn name
  [system]
  (components/get-config system :name))

(defn description
  [system]
  (components/get-config system :description))

(defn port
  [system]
  (components/get-config system :port))

(defn output-dir
  [system]
  (components/get-config system :output-dir))

(defn output-file-tmpl
  [system]
  (components/get-config system :output-file-tmpl))

(defn base-path
  [system]
  (components/get-config system :base-path))

(defn posts-path
  [system]
  (components/get-config system :posts-path))

(defn posts-path-src
  [system]
  (components/get-config system :posts-path-src))

(defn feed-count
  [system]
  (components/get-config system :feed-count))

(defn link-tmpl
  [system]
  (components/get-config system :link-tmpl))

(defn cli
  [system]
  (components/get-config system :cli))

(defn log-level
  [system]
  (components/get-config system :cli :log-level))

(defn log-nss
  [system]
  (components/get-config system :cli :log-nss))

(defn repl
  [system]
  (components/get-config system :repl))

(defn repl-log-level
  [system]
  (components/get-config system :repl :log-level))

(defn repl-log-nss
  [system]
  (components/get-config system :repl :log-nss))

(defn app
  [system]
  (components/get-config system :app))

(defn app-log-level
  [system]
  (components/get-config system :app :log-level))

(defn app-log-nss
  [system]
  (components/get-config system :app :log-nss))

(defn templating
  [system]
  (components/get-config system :templating))

(defn template-skip-marker
  [system]
  (components/get-config system :templating :skip-marker))

(defn parsing
  [system]
  (components/get-config system :parsing))

(defn word-separator
  [system]
  (components/get-config system :parsing :word-separator))

(defn word-joiner
  [system]
  (components/get-config system :parsing :word-joiner))

(defn paragraph-separator
  [system]
  (components/get-config system :parsing :paragraph-separator))

(defn tag-separator
  [system]
  (components/get-config system :parsing :tag-separator))

(defn sentence-end
  [system]
  (components/get-config system :parsing :sentence-end))

(defn ellipsis
  [system]
  (components/get-config system :parsing :ellipsis))

(defn period-ellipsis
  [system]
  (components/get-config system :parsing :period-ellipsis))

(defn db
  [system]
  (components/get-config system :db))

(defn db-type
  [system]
  (components/get-config system :db :type))

(defn db-config
  [system]
  ((db-type system) (db system)))

(defn db-conn
  [system]
  (:conn (db-config system)))

(defn db-start-config
  [system]
  (:start (db-config system)))

(defn db-version
  [system]
  (:version (db-config system)))

(defn db-start-delay
  [system]
  (get-in (db-config system) [:start :delay]))

(defn db-start-retry
  [system]
  (get-in (db-config system) [:start :retry-delay]))

(defn db-start-retry-timeout
  [system]
  (get-in (db-config system) [:start :retry-timeout]))
