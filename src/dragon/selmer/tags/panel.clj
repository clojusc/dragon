(ns dragon.selmer.tags.panel
  (:require
    [clojure.string :as string]
    [dragon.selmer.tags.flickr :as flickr]
    [dragon.selmer.tags.util :as tags-util]
    [taoensso.timbre :as log]
    [trifl.core :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- panel-html
  [css-class content]
  (format
    "<div class=\"panel panel-default %s\">%s</div>"
    css-class
    content))

(defn- panel-title-html
  [title]
  (format
    "<div class=\"panel-heading\"><h3 class=\"panel-title\">%s</h3></div>"
    title))

(defn- panel-body-html
  [content]
  (format
    "<div class=\"panel-body\">%s</div>"
    content))

(defn- panel-body-no-pad-html
  [content]
  (format
    "<div class=\"panel-body\" style=\"padding:0; margin: 0\">%s</div>"
    content))

(defn- panel-form-html
  [content]
  (format
    "<form class=\"form-horizontal\">%s</form>"
    content))

(defn- panel-table-html
  [content]
  (panel-body-html
    (panel-form-html content)))

(defn- panel-row-html
  [title content]
  (str
    "<div class=\"form-group\">"
    (format "<label for=\"panel-row\" class=\"col-sm-3 control-label\">%s</label>"
            title)
    "<div id=\"panel-row\" class=\"col-sm-9\">"
    tags-util/escaped-str-skip-marker
    (tags-util/stripped-md->html content)
    tags-util/escaped-str-skip-marker
    "</div>"
    "</div>"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Tags   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn tag
  [raw-args context-map content]
  (let [args (tags-util/args->map raw-args)]
    (panel-html
      (:class args)
      (tags-util/stripped-str
        (get-in content [:panel :content])))))

(defn body-tag
  [raw-args context-map content]
  (let [args (tags-util/args->map raw-args)]
    (str
      (panel-title-html (:title args))
      (panel-body-html
        (tags-util/stripped-md->html
          (get-in content [:panel-body :content]))))))

(defn table-tag
  [raw-args context-map content]
  (let [args (tags-util/args->map raw-args)]
    (str
      (panel-title-html (:title args))
      (panel-table-html (get-in content [:panel-table :content])))))

(defn row-tag
  [raw-args context-map content]
  (let [args (tags-util/args->map raw-args)]
    (panel-row-html (:title args) (get-in content [:panel-row :content]))))

(defn img-tag
  [raw-args context-map]
  (let [args (tags-util/args->map raw-args)]
    (str
      (panel-title-html (:title args))
      (panel-body-no-pad-html
        (format (str tags-util/escaped-format-skip-marker
                     "<img src=\"%s\">"
                     tags-util/escaped-format-skip-marker)
                (:src args))))))
