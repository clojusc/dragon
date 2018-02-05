(ns dragon.components.config
  (:require
    [com.stuartsierra.component :as component]
    [dragon.config.core :as config]
    [dragon.util :as util]
    [taoensso.timbre :as log])
  (:refer-clojure :exclude [name]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Config Component API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn extract-config
  ""
  [system args]
  (let [base-keys [:config :dragon]]
    (if-not (seq args)
      (get-in system base-keys)
      (get-in system (concat base-keys args)))))

(defn get-config
  [system & args]
  (let [cfg (extract-config system args)]
    (if (util/atom? cfg)
      @cfg
      cfg)))

(defn domain
  [system]
  (get-config system :domain))

(defn domain-urn
  [system]
  (->> system
       (domain)
       (util/dots->dashes)
       (format "urn:%s")))

(defn name
  [system]
  (get-config system :name))

(defn description
  [system]
  (get-config system :description))

(defn port
  [system]
  (get-config system :port))

(defn output-dir
  [system]
  (get-config system :output-dir))

(defn output-file-tmpl
  [system]
  (get-config system :output-file-tmpl))

(defn base-path
  [system]
  (get-config system :base-path))

(defn posts-path
  [system]
  (get-config system :posts-path))

(defn posts-path-src
  [system]
  (get-config system :posts-path-src))

(defn feed-count
  [system]
  (get-config system :feed-count))

(defn link-tmpl
  [system]
  (get-config system :link-tmpl))

(defn cli
  [system]
  (get-config system :cli))

(defn log-level
  [system]
  (get-config system :cli :log-level))

(defn log-nss
  [system]
  (get-config system :cli :log-nss))

(defn repl
  [system]
  (get-config system :repl))

(defn repl-log-level
  [system]
  (get-config system :repl :log-level))

(defn repl-log-nss
  [system]
  (get-config system :repl :log-nss))

(defn app
  [system]
  (get-config system :app))

(defn app-log-level
  [system]
  (get-config system :app :log-level))

(defn app-log-nss
  [system]
  (get-config system :app :log-nss))

(defn templating
  [system]
  (get-config system :templating))

(defn template-skip-marker
  [system]
  (get-config system :templating :skip-marker))

(defn blocks-enabled
  [system]
  (get-config system :blocks :enabled))

(defn parsing
  [system]
  (get-config system :parsing))

(defn word-separator
  [system]
  (get-config system :parsing :word-separator))

(defn word-joiner
  [system]
  (get-config system :parsing :word-joiner))

(defn paragraph-separator
  [system]
  (get-config system :parsing :paragraph-separator))

(defn tag-separator
  [system]
  (get-config system :parsing :tag-separator))

(defn sentence-end
  [system]
  (get-config system :parsing :sentence-end))

(defn ellipsis
  [system]
  (get-config system :parsing :ellipsis))

(defn period-ellipsis
  [system]
  (get-config system :parsing :period-ellipsis))

(defn robots-allow
  [system]
  (get-config system :robots :allow))

(defn robots-disallow
  [system]
  (get-config system :robots :disallow))

(defn db
  [system]
  (get-config system :db))

(defn db-type
  [system]
  (get-config system :db :type))

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

(defn processor-constructor
  [system]
  (get-config system :processor :constructor))

(defn workflow-type
  [system]
  (get-config system :workflow :type))

(defn workflow-storage
  [system]
  (get-config system :workflow :storage))

(defn workflow-qualifier
  [system]
  [(get-config system :workflow :type)
   (get-config system :workflow :storage)])

(defn apis
  [system]
  (get-config system :apis))

(defn flickr-api
  [system]
  (get-config system :apis :flickr))

(defn flickr-api-access-key
  [system]
  (-> system
      (get-config :apis :flickr :access)
      config/read-home-file))

(defn twitter-api-app-consumer-key
  [system]
  (-> system
      (get-config :apis :twitter :app-consumer :key)
      config/read-home-file))

(defn twitter-api-app-consumer-secret
  [system]
  (-> system
      (get-config :apis :twitter :app-consumer :secret)
      config/read-home-file))

(defn twitter-api-user-access-token
  [system]
  (-> system
      (get-config :apis :twitter :user-access :token)
      config/read-home-file))

(defn twitter-api-user-access-secret
  [system]
  (-> system
      (get-config :apis :twitter :user-access :secret)
      config/read-home-file))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Lifecycle Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord Config [builder dragon])

(defn start
  [this]
  (log/info "Starting config component ...")
  (log/debug "Started config component.")
  (let [cfg (:builder this)]
    (log/trace "Built configuration:" cfg)
    (reset! (:dragon this) cfg)
    (assoc this :dragon cfg)))

(defn stop
  [this]
  (log/info "Stopping config component ...")
  (log/debug "Stopped config component.")
  (assoc this :dragon nil))

(def lifecycle-behaviour
  {:start start
   :stop stop})

(extend Config
  component/Lifecycle
  lifecycle-behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-component
  ""
  [config-builder-fn]
  (map->Config
    {:builder config-builder-fn
     :dragon (atom {})}))
