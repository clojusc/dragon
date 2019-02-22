(ns dragon.components.config
  "Configuration component namespace."
  (:require
    [clojusc.config.unified.components.config :as config :refer [get-cfg]]
    [com.stuartsierra.component :as component]
    [dragon.util :as util]
    [taoensso.timbre :as log])
  (:refer-clojure :exclude [name]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Config Component API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Logging

(def log-color? config/log-color?)
(def log-level config/log-level)
(def log-nss config/log-nss)

(defn name
  [system]
  (:name (get-cfg system)))

(defn description
  [system]
  (:description (get-cfg system)))

(defn domain
  [system]
  (:domain (get-cfg system)))

(defn domain-urn
  [system]
  (->> (domain system)
       (util/dots->dashes)
       (format "urn:%s")))

(defn port
  [system]
  (:port  (get-cfg system)))

(defn output-dir
  [system]
  (get-in (get-cfg system) [:paths :output]))

(defn base-path
  [system]
  (get-in (get-cfg system) [:paths :base]))

(defn posts-path
  [system]
  (get-in (get-cfg system) [:paths :posts]))

(defn posts-path-src
  [system]
  (get-in (get-cfg system) [:paths :input]))

(defn output-file-tmpl
  [system]
  (get-in (get-cfg system) [:files :output-template]))

(defn feed-count
  [system]
  (get-in (get-cfg system) [:feed :count]))

(defn link-tmpl
  [system]
  (get-in (get-cfg system) [:links :template]))

(defn log-level
  [system]
  (get-in (get-cfg system) [:logging :level]))

(defn log-nss
  [system]
  (get-in (get-cfg system) [:logging :nss]))

(defn template-skip-marker
  [system]
  (get-in (get-cfg system) [:parsing :skip-marker]))

(defn template-config
  [system]
  {:output-file (output-file-tmpl system)
   :skip-marker (template-skip-marker system)
   :link (link-tmpl system)})

(defn blocks-enabled
  [system]
  (get-in (get-cfg system) [:blocks :enabled]))

(defn word-separator
  [system]
  (re-pattern
    (get-in (get-cfg system) [:parsing :word-separator])))

(defn word-joiner
  [system]
  (get-in (get-cfg system) [:parsing :word-joiner]))

(defn paragraph-separator
  [system]
  (re-pattern
    (get-in (get-cfg system) [:parsing :paragraph-separator])))

(defn tag-separator
  [system]
  (re-pattern
    (get-in (get-cfg system) [:parsing :tag-separator])))

(defn sentence-end
  [system]
  (get-in (get-cfg system) [:parsing :sentence-end]))

(defn ellipsis
  [system]
  (get-in (get-cfg system) [:parsing :ellipsis]))

(defn period-ellipsis
  [system]
  (get-in (get-cfg system) [:parsing :period-ellipsis]))

(defn robots-allow
  [system]
  (get-in (get-cfg system) [:robots :allow]))

(defn robots-disallow
  [system]
  (get-in (get-cfg system) [:robots :disallow]))

(defn db-type
  [system]
  (get-in (get-cfg system) [:db :type]))

(defn db-config
  [system]
  (get-in (get-cfg system) [:db (db-type system)]))

(defn db-conn
  [system]
  {:conn (db-config system)})

(defn db-version
  [system]
  (:version (db-config system)))

(defn processor-constructor
  [system]
  (get-in (get-cfg system) [:processor :constructor]))

(defn headlines-count
  [system]
  (get-in (get-cfg system) [:headlines :count]))

(defn default-images-headliner-tmpl
  [system]
  (get-in (get-cfg system) [:default-images :headliner]))

(defn default-images-post-tmpl
  [system]
  (get-in (get-cfg system) [:default-images :post]))

(defn default-images-small-tmpl
  [system]
  (get-in (get-cfg system) [:default-images :small]))

(defn default-images-thumb-tmpl
  [system]
  (get-in (get-cfg system) [:default-images :thumb]))

(defn default-images-count
  [system]
  (get-in (get-cfg system) [:default-images :count]))

(defn headlines-supporting-count
  [system]
  (get-in (get-cfg system) [:headlines :supporting :count]))

(defn headlines-middle-count
  [system]
  (get-in (get-cfg system) [:headlines :middle :count]))

(defn headlines-trailing-count
  [system]
  (get-in (get-cfg system) [:headlines :trailing :count]))

(defn headlines-trailing-rows
  [system]
  (get-in (get-cfg system) [:headlines :trailing :rows]))

(defn watcher-type
  [system]
  (get-in (get-cfg system) [:watcher :type]))

(defn watcher-content-dirs
  [system]
  (get-in (get-cfg system) [:watcher :content :dirs]))

(defn watcher-docs-dirs
  [system]
  (get-in (get-cfg system) [:watcher :docs :dirs]))

(defn watcher-sass-dirs
  [system]
  (get-in (get-cfg system) [:watcher :sass :dirs]))

(defn flickr-api-access-key
  [system]
  (-> (get-cfg system)
      (get-in [:apis :flickr :access])
      util/read-home-file))

(defn twitter-api-app-consumer-key
  [system]
  (-> (get-cfg system)
      (get-in [:apis :twitter :app-consumer :key])
      util/read-home-file))

(defn twitter-api-app-consumer-secret
  [system]
  (-> (get-cfg system)
      (get-in [:apis :twitter :app-consumer :secret])
      util/read-home-file))

(defn twitter-api-user-access-token
  [system]
  (-> (get-cfg system)
      (get-in [:apis :twitter :user-access :token])
      util/read-home-file))

(defn twitter-api-user-access-secret
  [system]
  (-> (get-cfg system)
      (get-in [:apis :twitter :user-access :secret])
      util/read-home-file))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Lifecycle Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Implemented in clojusc.config.unified.components.config

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Implemented in clojusc.config.unified.components.config
