(ns dragon.data.sources.impl.common
  (:require
    [clojure.java.shell :as shell]
    [dragon.components.config :as config]
    [dragon.util :as util]
    [taoensso.timbre :as log]))

(defn execute-db-command!
  [this]
  (let [;start-cfg (config/db-start-config (:component this))
        home (System/getProperty "user.dir")
        ; args (:args start-cfg)
        args []
        ]
    (shell/with-sh-dir home
      (log/debugf "Running command in %s ..." home)
      (log/debug "Using shell/sh args:" (vec args))
      (apply util/shell! args))))

(defn remove-connection
  [this]
  (assoc (:component this) :conn nil))

(def connection-behaviour
  {:execute-db-command! execute-db-command!
   :setup-schema :not-implemented
   :setup-subscribers :not-implemented
   :add-connection :not-implemented
   :remove-connection remove-connection})
