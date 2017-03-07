(ns dragon.web
  (:require [dragon.config :as config]
            [org.httpkit.server :as server]
            [stasis.core :as stasis]
            [taoensso.timbre :as log]))

(defn app
  "To be used with a routes definition like the following:
  ```clj
    {\"/index.html\" (page/front-page)
     \"/about.html\" (page/about)
     \"/about/credits.html\" (page/credits))})
  ```"
  [routes]
  (stasis/serve-pages routes))

(defn run
  [routes port]
  (log/infof (str "Starting development HTTP server on port %s "
                  "using dynamic content ...")
             port)
  (server/run-server
    (app routes)
    {:port port}))
