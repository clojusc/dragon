(ns dragon.generator
  (:require [dragon.config :as config]
            [dragon.web :as web]
            [stasis.core :as stasis]
            [taoensso.timbre :as log]))

(defn run
  [routes & args]
  (let [out-dir (config/get-output-dir)]
    (log/infof "Generating static content to %s ..." out-dir)
    (stasis/export-pages
      routes
      out-dir)
    (log/info "Static generation complete.")))
