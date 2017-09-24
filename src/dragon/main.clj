(ns dragon.main
  (:require [dragon.cli.core :as cli]
            [dragon.components.core :as components]
            [dragon.config :as config]
            [dragon.web.core :as web]
            [taoensso.timbre :as log]
            [trifl.java :as trifl])
  (:gen-class))

(defn -main
  "This is the entry point for Dragon.

  It manages both the running of the application and related services, as well
  as use of the application name spaces for running tasks on the comand line.

  The entry point is executed from the command line when calling `lein run`."
  ([]
   (-main :web))
  ([mode & args]
   (let [system (components/start)]
     (log/infof "Running Dragon in %s mode ..." mode)
     (log/debug "Passing the following args to the application:" args)
     (case (keyword mode)
       :web (web/run system)
       :cli (cli/run system (map keyword args)))
     (trifl/add-shutdown-handler #(components/stop system)))
   (shutdown-agents)))
