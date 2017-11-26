(ns dragon.selmer.tags.flickr
  (:require
    [cheshire.core :as json]
    [clj-http.client :as client]
    [clojure.string :as string]
    [dragon.config.core :as config]
    [dragon.selmer.tags.util :as tags-util]
    [taoensso.timbre :as log]
    [trifl.core :as util]))

(def api-endpoint "https://api.flickr.com/services/rest/")
(def api-method "flickr.photos.getSizes")
(def url-template "https://www.flickr.com/photos/%s/%s/in/album-%s")

(defn extract-sizes
  [sizes-payload]
  (-> sizes-payload
      (json/parse-string true)
      :sizes
      :size))

(defn- get-dimensions
  [dimension sizes]
  (map (comp tags-util/->int dimension) sizes))

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
  (apply min-key
        #(Math/abs (- % (tags-util/->int preferred-width)))
        (get-widths sizes)))

(defn get-best-size
  [preferred-width sizes]
  (->> sizes
       (filter #(= (get-best-width preferred-width sizes)
                   (tags-util/->int (:width %))))
       vec
       first))

(defn img-tag
  [raw-args context-map]
  (let [args (tags-util/args->map raw-args)
        sizes-data (call-get-sizes (:system context-map) (:photo-id args))
        best-size (get-best-size (:width args) sizes-data)]
    (log/debug "Parsed args:" args)
    (log/trace "context-map:" context-map)
    (log/trace "sizes-data:" sizes-data)
    (log/debug "preferred width:" (:width args))
    (log/debug "best available width:" (get-best-width (:width args) sizes-data))
    (log/debug "best-size:" best-size)
    (format (str tags-util/escaped-format-skip-marker
                 "<a href=\""
                 url-template
                 "\">"
                 "<img src=\"%s\" style=\"width:%spx;height:auto;\">"
                 "</a>"
                 tags-util/escaped-format-skip-marker)
      (:user args)
      (:photo-id args)
      (:album-id args)
      (:source best-size)
      (:width args))))
