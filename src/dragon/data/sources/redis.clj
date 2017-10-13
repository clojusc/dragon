(ns dragon.data.sources.redis
  (:require [clojure.java.io :as io]
            [dragon.config :as config]
            [dragon.data.sources.core :as db-core]
            [dragon.util :as util]
            [taoensso.carmine :as car :refer [wcar]]
            [taoensso.timbre :as log]
            [trifl.fs :as fs]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Constants & Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def redis-schemas
  ;; Keys  & val types:
  ;; * path-segment:content / string
  ;; * path-segment:metadata / hash (binary)
  ;; * path-segment:stats / hash (binary)
  ;; * all-posts:metadata / hash (binary)
  ;; * all-posts:stats / hash (binary)
  )

(defn cmd
  "With this function we can do things like the following in the REPL (for
  querying Redis):

  ```clj
  => (redis/cmd 'ping)
  => (redis/cmd 'get \"testkey\")
  => (redis/cmd 'set \"foo\" \"bar\")
  ```

  (Note that the escaped strings are for the docstring, and not what you'd
  actually type in the REPL.)"
  [component-or-system cmd & args]
  (car/wcar (:conn (config/db-config component-or-system))
            (apply (resolve (symbol (str "car/" cmd))) args)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dragon DB API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn start-db!
  [component]
  (db-core/execute-db-command! component))

(defn setup-schemas
  [component]
  )

(defn setup-subscribers
  [component]
  )

(defn add-connection
  [component]
  (assoc component :conn (config/db-conn component)))

(defn stop-db!
  [component]
  (let [id-file (:container-id-file (config/db-config component))]
    (->> (slurp id-file)
         (util/shell! "docker" "stop")
         vec)
    (when (fs/exists? (io/as-file id-file))
      (util/shell! "rm" id-file))))
