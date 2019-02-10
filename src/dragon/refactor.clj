(ns dragon.refactor
	(:require
		[clojure.string :as string]
		[dragon.blog.core :as blog]
		[dragon.blog.content.core :as content]
		[dragon.blog.post.core :as post]
		[dragon.blog.post.util :as post-util]
		[dragon.blog.workflow.core :as workflow]
		[dragon.components.config :as config]
		[dragon.data.sources.core :as db]
		[dragon.data.sources.impl.redis :refer [schema]]
		[dragon.util :as util]
		[taoensso.timbre :as log]))

(defn process-file
	[system file data src-file checksum]
	(log/infof "Changed detected; processing %s ..." src-file)
	(let [querier (get-in system [:db :querier])
		    processor (post/new-processor system)
		    src-dir (.getParent file)
		    filename-old (.getName file)
		    filename (format (config/output-file-tmpl system)
                         (util/sanitize-str (:title data)))
		    metadata (dissoc data :body :body-orig :tags :category)
		    uri-path (-> src-file
                     (string/replace filename-old filename)
                     (util/sanitize-post-path)
                     (string/replace-first "posts/" ""))
		    tags (post/get-tags processor (:tags data))
		    dates (post/get-dates processor src-file)
		    stats (post/get-stats processor (:body data))
		    excerpts (post/get-excerpts processor (:body data))]
		(log/trace "Got data:" data)
		(log/trace "Got checksum:" checksum)
		(log/trace "Got dates:" dates)
		(log/trace "Got excerpts:" excerpts)
		(log/trace "Got filename:" filename)
		(log/trace "Got metadata:" metadata)
		(log/trace "Got src-dir:" src-dir)
		(log/trace "Got stats:" stats)
		(log/trace "Got tags:" tags)
		(log/infof "Post %s will be accessible at: %s" (:title metadata) uri-path)
		(db/set-post-category querier src-file (:category data))
		(db/set-post-checksum querier src-file checksum)
		(db/set-post-content querier src-file (:body data))
		(db/set-post-dates querier src-file dates)
		(db/set-post-excerpts querier src-file excerpts)
		(db/set-post-metadata querier src-file metadata)
		(db/set-post-stats querier src-file stats)
		(db/set-post-tags querier src-file tags)
		(db/set-post-uri-path querier src-file uri-path)))

(defn process-files
	[system]
	(doseq [file (sort (blog/get-files (config/posts-path-src system)))]
		(let [src-file (.getPath file)
			    _ (log/infof "Checking source file %s ..." src-file)
			    data (->> file
					          (content/parse system)
					          (post-util/copy-original-body))
			    checksum (util/check-sum (pr-str data))
			    querier (get-in system [:db :querier])]
			(if (db/post-changed? querier src-file checksum)
				(process-file system file data src-file checksum)
				(log/infof "File %s has already been processed; skipping ..."
					         src-file))))
	:ok)
