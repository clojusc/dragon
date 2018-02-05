(ns dragon.config.datomic
  (:require
    [datomic.client :as datomic]))

(def ^:private base-config
  {:version "0.9.5561.62"
   :host "localhost"
   :port "8998"
   :access-key "dragon"
   :secret "dragon"
   :db-name "dragon"})

(def ^:private start
  {:delay 5000
   :retry-delay 500
   :retry-timeout 10000
   :home (str "/opt/datomic/" (:version base-config))
   :executable "bin/run"
   :entry-point "datomic.peer-server"
   :host (:host base-config)
   :port (:port base-config)
   :db (format "%s,datomic:mem://%s" (:db-name base-config)
                                    (:db-name base-config))
   :auth (format "%s,%s" (:access-key base-config)
                           (:secret base-config))})

(def ^:private start-args
  [(:executable start)
   "-m" (:entry-point start)
   "-h" (:host start)
   "-p" (:port start)
   "-d" (:db start)
   "-a" (:auth start)])

(def config
  {:version (:version base-config)
   :start (assoc start :args start-args)
   :conn {
     :account-id datomic/PRO_ACCOUNT
     :region datomic/PRO_REGION
     :service "peer-server"
     :endpoint (format "%s:%s" (:host base-config)
                               (:port base-config))
     :db-name (:db-name base-config)
     :access-key (:access-key base-config)
     :secret (:secret base-config)}})
