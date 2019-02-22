(ns dragon.blog.generator
  (:require
    [dragon.components.config :as config]
    [stasis.core :as stasis]
    [taoensso.timbre :as log]))

(defn run
  ([system]
   (run system []))
  ([system routes]
   (let [out-dir (config/output-dir system)]
     (when-not (seq routes)
       (log/warn "Routes are manually set to be empty! Please fix."))
     (log/infof "Writing generated content to '%s' ..." out-dir)
     (stasis/export-pages routes out-dir)
     (log/info "Static generation complete."))
   :ok))
