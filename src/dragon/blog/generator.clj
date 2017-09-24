(ns dragon.blog.generator
  (:require [dragon.config :as config]
            [stasis.core :as stasis]
            [taoensso.timbre :as log]))

(defn run
  [system]
  (let [routes []
        out-dir (config/output-dir system)]
    (log/infof "Generating static content to '%s' ..." out-dir)
    (log/warn "Routes are manually set to be empty! Please fix.")
    (stasis/export-pages routes out-dir)
    (log/info "Static generation complete.")))
