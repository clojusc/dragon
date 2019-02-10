(ns dragon.refactor
	(:require
		[clojure.string :as string]
		[dragon.blog.core :as blog]
		[dragon.blog.post.core :as post]
		[dragon.components.config :as config]
		[dragon.components.db :as db-component]
		[dragon.data.sources.core :as db]
		[dragon.data.sources.impl.redis :refer [schema]]
		[dragon.util :as util]
		[taoensso.timbre :as log]))

(defn process-file
	[processor querier file data opts]
	(log/infof "Changed detected; processing %s ..." (:src-file opts))
	(let [src-dir (.getParent file)
		    filename-old (.getName file)
		    metadata (dissoc data :body :body-orig :tags :category)
		    uri-path (-> (:src-file opts)
                     (string/replace filename-old (:filename opts))
                     (util/sanitize-post-path)
                     (string/replace-first "posts/" ""))

		    tags (post/get-tags processor (:tags data) (:tag-separator opts))
		    dates (post/get-dates processor (:src-file opts))
		    stats (post/get-stats processor (:body data))
		    excerpts (post/get-excerpts processor (:body data))]
		(log/trace "Got data:" data)
		(log/trace "Got dates:" dates)
		(log/trace "Got excerpts:" excerpts)
		(log/trace "Got metadata:" metadata)
		(log/trace "Got src-dir:" src-dir)
		(log/trace "Got stats:" stats)
		(log/trace "Got tags:" tags)
		(log/infof "Post %s will be accessible at: %s" (:title metadata) uri-path)
		{:category (:category data)
		 :checksum (:checksum opts)
		 :content (:body data)
		 :content-source (:body-orig data)
		 :dates dates
		 :excerpts excerpts
		 :metadata metadata
		 :stats stats
		 :tags tags
		 :uri-path uri-path}))

(defn set-file-data
	[querier src-file {:keys [category checksum content content-source dates
		               					excerpts metadata stats tags uri-path]}]
	(db/set-post-category querier src-file category)
	(db/set-post-checksum querier src-file checksum)
	(db/set-post-content querier src-file content)
	(db/set-post-content-source querier src-file content-source)
	(db/set-post-dates querier src-file dates)
	(db/set-post-excerpts querier src-file excerpts)
	(db/set-post-metadata querier src-file metadata)
	(db/set-post-stats querier src-file stats)
	(db/set-post-tags querier src-file tags)
	(db/set-post-uri-path querier src-file uri-path))

(defn process-files
	[system]
	(doseq [file (sort (blog/get-files (config/posts-path-src system)))]
		(let [src-file (.getPath file)
			    _ (log/infof "Checking source file %s ..." src-file)
			    processor (post/new-processor system)
			    querier (db-component/db-querier system)
			    tmpl-cfg (config/template-config system)
			    data (post/get-data processor file tmpl-cfg)
			    checksum (util/check-sum (pr-str data))
			    filename (format (config/output-file-tmpl system)
                         	 (util/sanitize-str (:title data)))
			    opts {:tag-separator (config/tag-separator system)
			    	    :checksum checksum
			    	    :src-file src-file
			    	    :filename filename}]
			(log/debug "Got checksum:" (:checksum opts))
			(log/debug "Got filename:" (:filename opts))
			(if (db/post-changed? querier src-file checksum)
				(set-file-data
					querier
					src-file
					(process-file processor querier file data opts))
				(log/infof "File %s has already been processed; skipping ..."
					         src-file))))
	:ok)
