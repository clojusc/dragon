(ns dragon.web
  (:require [dragon.config :as config]
            [org.httpkit.server :as server]
            [ring.middleware.file :as ring-file]
            [stasis.core :as stasis]
            [taoensso.timbre :as log]))

(defn run
  ([stasis-routes port]
    (run stasis-routes port "docs"))
  ([stasis-routes port docroot]
    (log/infof
      (str "Starting development HTTP server on port %s using dynamic "
           "content ...")
      port)
    (server/run-server
      (ring-file/wrap-file {} docroot)
      {:port port})))
