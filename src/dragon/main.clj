(ns dragon.main
  (:require [clojusc.twig :as logger]
            [dragon.cli.core :as cli]
            [dragon.components.system :as components]
            [dragon.config.core :refer [build]
                                :rename {build build-config}]
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
  [mode args]
  (log/debug "Getting context-senstive system ...")
  (log/trace "Got mode:" mode)
  (log/trace "Got args:" (vec args))
  (cond
    (contains? (set args) :help) (do
                                   ;; Only log errors in help mode
                                   (logger/set-level! '[dragon] :error)
                                   (components/start build-config :basic))
    (= :run (first args)) (do
                            ;; Run logging quietly until the logging component
                            ;; starts up
                            (logger/set-level! '[dragon] :debug)
                            (components/start build-config :web))
    :else (do
            ;; Run logging quietly until the logging component
            ;; starts up
            (logger/set-level! '[dragon] :debug)
            (components/start build-config :cli))))

(defn -main
  "This is the entry point for Dragon.

  It manages both the running of the application and related services, as well
  as use of the application name spaces for running tasks on the comand line.

  The entry point is executed from the command line when calling `lein run`."
  [mode & raw-args]
  (let [args (get-default-args raw-args)
        system (get-context-sensitive-system mode args)]
   (log/infof "Running Dragon in %s mode ..." mode)
   (log/debug "Passing the following args to the application:" args)
   (case (keyword mode)
     :cli (cli/run system (map keyword args)))
   ;; Do a full shut-down upon ^c
   (trifl/add-shutdown-handler #(components/stop system))))
