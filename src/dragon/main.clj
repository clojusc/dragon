(ns dragon.main
  (:require [clojusc.twig :as logger]
            [dragon.cli :as cli]
            [dragon.config :as config]
            [dragon.web :as web]
            [taoensso.timbre :as log])
  (:gen-class))

(defn -main
  "This is the entry point for the Clojang blog Application.

  It manages both the running of the application and related services, as well
  as use of the application name spaces for running tasks on the comand line.

  The entry point is executed from the command line when calling `lein run`."
  ([]
    (-main :web))
  ([mode & args]
    ;; Set the initial log-level before the components set the log-levels for
    ;; the configured namespaces
    (logger/set-level! ['dragon] (config/log-level))
    (log/infof "Running the Clojang blog application in %s mode ..." mode)
    (log/debug "Passing the following args to the application:" args)
    (case (keyword mode)
      :web (web/run)
      :cli (cli/run (map keyword args)))))
