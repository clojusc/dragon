(ns dragon.config.redis)

(def ^:private base-config
  {:container-id-file "/tmp/redis-dragon-docker-id"
   :image-name "redis:4.0.2-alpine"
   :host "localhost"
   :port "6379"
   :host-data-dir "data"
   :guest-data-dir "/data"})

(def ^:private start
  {:home (System/getProperty "user.dir")
   :executable "docker"
   :command "redis-server"})

(def ^:private start-args
  [(:executable start)
   "run"
   "-d"
   "-v" (format "%s/%s:%s" (:home start)
                           (:host-data-dir base-config)
                           (:guest-data-dir base-config))
   "--cidfile" (:container-id-file base-config)
   "-p" (format "%s:%s" (:port base-config) (:port base-config))
   (:image-name base-config)
   (:command start)
   "--appendonly" "yes"])

(def config
  {:start (assoc start :args start-args)
   :container-id-file (:container-id-file base-config)
   :conn {
     :pool {}
     :spec {
       :host (:host base-config)
       :port (Integer/parseInt (:port base-config))}}})
