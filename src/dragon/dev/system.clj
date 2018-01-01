(ns dragon.dev.system
  "FRMX Blog development namespace

  This namespace is particularly useful when doing active development on the
  FRMX Blog application."
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.namespace.repl :as repl]
    [clojusc.twig :as logger]
    [com.stuartsierra.component :as component]
    [dragon.blog.core :as blog]
    [dragon.blog.post.core :as post]
    [dragon.cli.core :as dragon-cli]
    [dragon.cli.show.posts :as cli-show-posts]
    [dragon.data.sources.core :as data-source]
    [dragon.data.sources.impl.redis :as redis-db]
    [dragon.main :as dragon-main]
    [dragon.util :as dragon-util]
    [selmer.parser :as selmer]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   State & Transition Vars   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic state :stopped)
(def ^:dynamic system nil)
(def ^:dynamic generator-ns "")
(def ^:dynamic system-ns "")
(def valid-stop-transitions #{:started :running})
(def invalid-init-transitions #{:initialized :started :running})
(def invalid-deinit-transitions #{:started :running})
(def invalid-start-transitions #{:started :running})
(def invalid-stop-transitions #{:stopped})
(def invalid-startup-transitions #{:running})
(def invalid-shutdown-transitions #{:uninitialized :shutdown})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Initial Setup & Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(selmer/cache-off!)
(logger/set-level! ['dragon] :info)

(defn resolve-by-name
  [an-ns a-fun]
  (println "DEBUG: got args: " an-ns a-fun)
  (resolve (symbol (str an-ns "/" a-fun))))

(defn call-by-name
  [an-ns a-fun & args]
  (println "DEBUG: got args: " an-ns a-fun args)
  (apply (resolve-by-name an-ns a-fun) args))

(defn redis
  [& args]
  (apply redis-db/cmd (concat [system] args)))

(defn set-generator-ns
  [an-ns]
  (alter-var-root #'generator-ns (constantly an-ns)))

(defn set-system-ns
  [an-ns]
  (alter-var-root #'system-ns (constantly an-ns)))

(defn set-system
  [value]
  (alter-var-root #'system (constantly value)))

(defn set-state
  [value]
  (alter-var-root #'state (constantly value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   State Management   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init
  ([]
    (init :default))
  ([mode]
    (if (contains? invalid-init-transitions state)
      (log/warn "System has aready been initialized.")
      (do
        (set-system (call-by-name system-ns "init"))
        (set-state :initialized)))
    state))

(defn deinit
  []
  (if (contains? invalid-deinit-transitions state)
    (log/error "System is not stopped; please stop before deinitializing.")
    (do
      (set-system nil)
      (set-state :uninitialized)))
  state)

(defn start
  ([]
    (start :default))
  ([mode]
    (when (nil? system)
      (init mode))
    (if (contains? invalid-start-transitions state)
      (log/warn "System has already been started.")
      (do
        (set-system (component/start system))
        (set-state :started)))
    state))

(defn stop
  []
  (if (contains? invalid-stop-transitions state)
    (log/warn "System already stopped.")
    (do
      (set-system (component/stop system))
      (set-state :stopped)))
  state)

(defn restart
  []
  (stop)
  (start))

(defn startup
  []
  "Initialize a system and start all of its components.

  This is essentially a convenience wrapper for `init` + `start`."
  (if (contains? invalid-startup-transitions state)
    (log/warn "System is already running.")
    (do
      (when (not (contains? invalid-init-transitions state))
        (init))
      (when (not (contains? invalid-start-transitions state))
        (start))
      (set-state :running)
      state)))

(defn shutdown
  []
  "Stop a running system and de-initialize it.

  This is essentially a convenience wrapper for `stop` + `deinit`."
  (if (contains? invalid-shutdown-transitions state)
    (log/warn "System is already shutdown.")
    (do
      (when (not (contains? invalid-stop-transitions state))
        (stop))
      (when (not (contains? invalid-deinit-transitions state))
        (deinit))
      (set-state :shutdown)
      state)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Reloading Management   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -refresh
  ([]
    (repl/refresh))
  ([& args]
    (apply #'repl/refresh args)))

(defn refresh
  "This is essentially an alias for clojure.tools.namespace.repl/refresh."
  [& args]
  (if (contains? valid-stop-transitions state)
    (stop))
  (apply -refresh args))

(defn reset
  []
  (stop)
  (deinit)
  (refresh :after 'dragon.dev.system/startup))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn show-lines-with-error
  "Process posts and show the lines of text that threw exceptions."
  ([]
    (show-lines-with-error #'system))
  ([system]
    (let [processor (post/new-processor system)]
      (->> processor
           (blog/get-posts)
           (map #(->> %
                     (post/get-data processor)
                     :text))
           (pprint)))))

(defn show-posts
  []
  (cli-show-posts/run system))

(defn generate
  []
  (call-by-name generator-ns "generate" system))

(defn force-regenerate
  []
  (call-by-name generator-ns "force-regenerate" system))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Aliases   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def reload #'reset)
