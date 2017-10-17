(ns dragon.data.sources.impl.common
  (:require [clojure.java.shell :as shell]
            [dragon.config :as config]
            [dragon.util :as util]
            [taoensso.timbre :as log]))

(defn execute-db-command!
  [this]
  (let [start-cfg (config/db-start-config (:component this))
        home (:home start-cfg)
        args (:args start-cfg)]
    (shell/with-sh-dir home
      (log/debugf "Running command in %s ..." home)
      (log/debug "Using shell/sh args:" args)
      (apply util/shell! args))))

(defn remove-connection
  [this]
  (assoc (:component this) :conn nil))

(def connection-behaviour
  {:start-db! execute-db-command!
   :execute-db-command! execute-db-command!
   :setup-schemas :not-implemented
   :setup-subscribers :not-implemented
   :add-connection :not-implemented
   :stop-db! :not-implemented
   :remove-connection remove-connection})
