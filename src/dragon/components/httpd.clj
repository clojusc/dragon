(ns dragon.components.httpd
  (:require [com.stuartsierra.component :as component]
            [dragon.components.core :as component-api]
            [dragon.components.config :as config]
            [dragon.event.subscription :as subscription]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.event.topic :as topic]
            [org.httpkit.server :as server]
            [ring.middleware.file :as ring-file]
            [taoensso.timbre :as log]))

(defrecord HTTPD []
  component/Lifecycle

  (start [component]
    (log/info "Starting httpd component ...")
    (let [port (config/port component)
          docroot (config/output-dir component)
          main-handler {}
          site (ring-file/wrap-file main-handler docroot)
          server (server/run-server site {:port port})]
      (log/debugf "Serving files from %s and listening on port %s"
                  docroot port)
      (log/debug "Started httpd component.")
      (assoc component :server server)))

  (stop [component]
    (log/info "Stopping httpd component ...")
    (if-let [server (:server component)]
      (server))
    (assoc component :server nil)))

(defn create-httpd-component
  ""
  []
  (->HTTPD))
