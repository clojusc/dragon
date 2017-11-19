(ns dragon.selmer.tags.flickr
  (:require
    [cheshire.core :as json]
    [clj-http.client :as client]
    [clojure.string :as string]
    [clojure.walk :as walk]
    [dragon.config.core :as config]
    [taoensso.timbre :as log]
    [trifl.core :as util]))

(def escaped-skip-marker
  "Once we generate HTML that we don't want to be re-interpreted by Selmer in
  any future/additional passes, we need to escape it."
  "%%%%%%")
(def api-endpoint "https://api.flickr.com/services/rest/")
(def api-method "flickr.photos.getSizes")
(def str-keys->kwd-keys
  {":user" :user
   ":photo-id" :photo-id
   ":album-id" :album-id
   ":height" :height
   ":width" :width})
(def url-template "https://www.flickr.com/photos/%s/%s/in/album-%s")

(defn ->int
  [arg]
  (if (integer? arg)
    arg
    (Integer/parseInt arg)))

(defn args->map
  [args]
  (->> args
       (partition 2)
       (map vec)
       (into {})
       (walk/postwalk-replace str-keys->kwd-keys)))

(defn extract-sizes
  [sizes-payload]
  (-> sizes-payload
      (json/parse-string true)
      :sizes
      :size))

(defn- get-dimensions
  [dimension sizes]
  (map (comp ->int dimension) sizes))

(defn get-widths
  [sizes]
  (get-dimensions :width sizes))

(defn get-heights
  [sizes]
  (get-dimensions :height sizes))

(defn call-get-sizes
  [system photo-id]
  (->> {:query-params {"method" api-method
                       "api_key" (config/flickr-api-access-key system)
                       "photo_id" photo-id
                       "format" "json"
                       "nojsoncallback" 1}}
        (client/get api-endpoint)
        :body
        extract-sizes
        vec))

(defn get-best-width
  [preferred-width sizes]
  (apply min-key #(Math/abs (- % (->int preferred-width))) (get-widths sizes)))

(defn get-best-size
  [preferred-width sizes]
  (->> sizes
       (filter #(= (get-best-width preferred-width sizes) (->int (:width %))))
       vec
       first))

(defn img-tag [raw-args context-map]
  (let [args (args->map raw-args)
        sizes-data (call-get-sizes (:system context-map) (:photo-id args))
        best-size (get-best-size (:width args) sizes-data)]
    (log/debug "Parsed args:" args)
    (log/trace "context-map:" context-map)
    (log/trace "sizes-data:" sizes-data)
    (log/debug "preferred width:" (:width args))
    (log/debug "best available width:" (get-best-width (:width args) sizes-data))
    (log/debug "best-size:" best-size)
    (format (str escaped-skip-marker
                 "<a href=\""
                 url-template
                 "\">"
                 "<img src=\"%s\" style=\"width:%spx;height:auto;\">"
                 "</a>"
                 escaped-skip-marker)
      (:user args)
      (:photo-id args)
      (:album-id args)
      (:source best-size)
      (:width args))))
