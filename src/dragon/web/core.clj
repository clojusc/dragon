(ns dragon.web.core
  (:require [dragon.config :as config]
            [org.httpkit.server :as server]
            [ring.middleware.file :as ring-file]
            [taoensso.timbre :as log]))

(defn run
  [system stasis-routes]
  (log/infof
   (str "Starting development HTTP server on port %s using dynamic "
        "content ...")
   (config/port system))
  (server/run-server
   (ring-file/wrap-file {} (config/output-dir system))
   {:port (config/port system)}))
