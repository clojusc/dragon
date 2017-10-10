(ns dragon.data.sources.core
  (:require [clojure.java.shell :as shell]
            [dragon.config :as config]
            [taoensso.timbre :as log]))

(defn execute-db-command!
  [component]
  (let [start-cfg (config/db-start-config component)
        home (:home start-cfg)
        args (:args start-cfg)]
    (shell/with-sh-dir home
      (log/debugf "Running command in %s ..." home)
      (log/debug "Using shell/sh args:" args)
      (apply shell/sh args))))

(defn remove-connection
  [component]
  (assoc component :conn nil))
