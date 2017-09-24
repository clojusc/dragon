(ns dragon.generator
  (:require [dragon.config :as config]
            [stasis.core :as stasis]
            [taoensso.timbre :as log]))

(defn run
  [system routes out-dir]
  (log/infof "Generating static content to '%s' ..." out-dir)
  (stasis/export-pages routes out-dir)
  (log/info "Static generation complete."))
