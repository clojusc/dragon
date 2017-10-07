(ns dragon.data.schemas.subscriber-registry)

(def topic
  {:db/ident :registry/topic
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one
   :db/doc "The topic with which the pubsub was created."})

(def tag
  {:db/ident :registry/tag
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one
   :db/doc "The tag given to a message."})

(def subscribers
  {:db/ident :registry/subscribers
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/many
   :db/doc "The subscribers for a message tag."})

(def schema [topic tag subscribers])
