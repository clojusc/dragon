(ns dragon.blog.generator
  (:require [dragon.config :as config]
            [stasis.core :as stasis]
            [taoensso.timbre :as log]))

(defn run
  ([system]
   (run system []))
  ([system routes]
   (let [out-dir (config/output-dir system)]
     (when-not (seq routes)
       (log/warn "Routes are manually set to be empty! Please fix."))
     (log/infof "Generating static content to '%s' ..." out-dir)
     (stasis/export-pages routes out-dir)
     (log/info "Static generation complete."))))
