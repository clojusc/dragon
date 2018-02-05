(ns dragon.components.httpd
  (:require
    [com.stuartsierra.component :as component]
    [dragon.components.config :as config]
    [dragon.event.subscription :as subscription]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [dragon.event.topic :as topic]
    [org.httpkit.server :as server]
    [ring.middleware.file :as ring-file]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   HTTP Server Component API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Lifecycle Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord HTTPD [])

(defn start
  [this]
  (log/info "Starting httpd component ...")
  (let [port (config/port this)
        docroot (config/output-dir this)
        main-handler {}
        site (ring-file/wrap-file main-handler docroot)
        server (server/run-server site {:port port})]
    (log/debugf "Serving files from %s and listening on port %s"
                docroot port)
    (log/debug "Started httpd component.")
    (assoc this :server server)))

(defn stop
  [this]
  (log/info "Stopping httpd component ...")
  (if-let [server (:server this)]
    (server))
  (assoc this :server nil))

(def lifecycle-behaviour
  {:start start
   :stop stop})

(extend HTTPD
  component/Lifecycle
  lifecycle-behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-component
  ""
  []
  (->HTTPD))
