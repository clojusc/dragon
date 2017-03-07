(ns dragon.cli
  (:require [clojure.pprint :refer [pprint]]
            [clojusc.twig :as logger]
            [dragon.cli.new :as new]
            [dragon.cli.show :as show]
            [dragon.generator :as gen]
            [dragon.util :as util]
            [dragon.web :as web]
            [taoensso.timbre :as log]
            [trifl.core :refer [sys-prop]]
            [trifl.docs :as docs]))

(defn help-cmd
  [& args]
  (docs/print-docstring 'dragon.cli 'run))

(defn version-cmd
  []
  (let [version (sys-prop "dragon.version")
        build (util/get-build)]
    (print (format "Dragon version %s, build %s\n" version build))))

(defn run
  "
  Usage:
  ```
    dragon COMMAND [help | arg...]
    dragon [-h | --help | -v | --version]
  ```

  Commands:
  ```
    new      Create files of a given type; see 'dragon new help'
    show     Display various blog data in the terminal
    gen      Generate updated static content for a site
    run      Run the dragon site locally as a Ring app
    help     Display this usage message
    version  Display the current dragon version
  ```

  More information:

    Each command takes an optional 'help' subcommand that will provide
    usage information about the particular command in question, e.g.:

  ```
    $ dragon new help
  ```"
  [[cmd & args]]
  (log/debug "Got cmd:" cmd)
  (log/debug "Got args:" args)
  (case cmd
    :new (new/run args)
    :show (show/run args)
    :gen (gen/run args)
    :run (web/run)
    :help (help-cmd args)
    :version (version-cmd)
    ;; Aliases
    :--help (help-cmd args)
    :--version (version-cmd)
    :-h (help-cmd args)
    :-v (version-cmd))
  (shutdown-agents))
