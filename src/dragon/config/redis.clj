(ns dragon.config.redis)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Common Configuration   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private base-config
  {:host "localhost"
   :port "6379"
   :host-data-dir "data"
   :guest-data-dir "/data"})

(def ^:private start
  {:home (System/getProperty "user.dir")})

(def ^:private redis-options
  ["--appendonly" "yes"])

(def config
  {:conn {
     :pool {}
     :spec {
       :host (:host base-config)
       :port (Integer/parseInt (:port base-config))}}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Docker-based Redis   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private redis-docker-version
  "4.0.2")

(def ^:private base-config-docker
  (assoc base-config
         :container-id-file "/tmp/redis-dragon-docker-id"
         :image-name (format "redis:%s-alpine" redis-docker-version)))

(def ^:private start-docker
  (assoc start
         :executable "docker"
         :command "redis-server"))

(def ^:private start-args-docker
  [(:executable start-docker)
   "run"
   "-d"
   "-v" (format "%s/%s:%s" (:home start-docker)
                           (:host-data-dir base-config-docker)
                           (:guest-data-dir base-config-docker))
   "--cidfile" (:container-id-file base-config-docker)
   "-p" (format "%s:%s" (:port base-config-docker) (:port base-config-docker))
   (:image-name base-config-docker)
   (:command start-docker)
   "--appendonly" "yes"])

(def config-docker
  (assoc config
         :start (assoc start-docker :args start-args-docker)
         :container-id-file (:container-id-file base-config-docker)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Native Redis   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
