(ns dragon.cli.core
  (:require [clojure.pprint :refer [pprint]]
            [clojusc.twig :as logger]
            [dragon.blog.generator :as gen]
            [dragon.cli.new.core :as new]
            [dragon.cli.show.core :as show]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.util :as util]
            [taoensso.timbre :as log]
            [trifl.core :refer [sys-prop]]
            [trifl.docs :as docs]))

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
  [system [cmd & args]]
  (log/debug "Got cmd:" cmd)
  (log/debug "Got args:" args)
  (event/publish system tag/run-cli {:cmd cmd :args args})
  (case cmd
    :new (new/run system args)
    :show (show/run system args)
    :gen (gen/run system)
    :help (docs/print-docstring #'run)
    :run (gen/run system)
    :version (version-cmd)
    ;; Aliases
    :--help (docs/print-docstring #'run)
    :--version (version-cmd)
    :-h (docs/print-docstring #'run)
    :-v (version-cmd))
  (event/publish system tag/shutdown-cli))
