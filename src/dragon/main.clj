(ns dragon.main
  (:require
    [clojusc.twig :as logger]
    [com.stuartsierra.component :as component]
    [dragon.cli.core :as cli]
    [dragon.components.core :as components]
    [dragon.config :as config-lib]
    [taoensso.timbre :as log]
    [trifl.java :as trifl])
  (:gen-class))

(defn get-default-args
  [raw-args]
  (let [args (map keyword raw-args)]
    (if (seq args)
      args
      [:run])))

(defn get-context-sensitive-system
  [mode cfg-data args]
  (log/debug "Getting context-sensitive system ...")
  (log/trace "Got mode:" mode)
  (log/trace "Got args:" (vec args))
  (cond
    (contains? (set args) :help)
    (do
      ;; Only log errors in help mode
      (logger/set-level! '[dragon] :error)
      (component/start (components/init :basic cfg-data)))

    (= :run (first args))
    (do
      ;; Run logging quietly until the logging component
      ;; starts up
      (logger/set-level! '[dragon] :debug)
      (component/start (components/init :web cfg-data)))

    :else
    (do
      ;; Run logging quietly until the logging component
      ;; starts up
      (logger/set-level! '[dragon] :debug)
      (component/start (components/init :cli cfg-data)))))

(defn -main
  "This is the entry point for Dragon

  It manages both the running of the application and related services, as well
  as use of the application name spaces for running tasks on the comand line.

  The entry point is executed from the command line when calling `lein run`."
  [mode & raw-args]
  (let [args (get-default-args raw-args)
        cfg-data (config-lib/data)
        system (get-context-sensitive-system mode cfg-data args)]
   (log/infof "Running Dragon in %s mode ..." mode)
   (log/debug "Passing the following args to the application:" args)
   (case (keyword mode)
     :cli (cli/run system (map keyword args)))
   ;; Do a full shut-down upon ^c
   (trifl/add-shutdown-handler #(component/stop system))))
