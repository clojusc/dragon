(ns dragon.event.tag)

(def read-config-pre ::read-config-pre)
(def read-config ::read-config)
(def read-config-post ::read-config-post)

(def subscribers-added-pre ::subscribers-added-pre)
(def subscribers-added ::subscribers-added)
(def subscribers-added-post ::subscribers-added-post)

(def run-cli-pre ::run-cli-pre)
(def run-cli ::run-cli)
(def run-cli-post ::run-cli-post)

(def process-one-pre ::process-one-pre)
(def process-one ::process-one)
(def process-one-post ::process-one-post)

(def process-all-pre ::process-all-pre)
(def process-all ::process-all)
(def process-all-post ::process-all-post)

(def read-source-pre ::read-source-pre)
(def read-source ::read-source)
(def read-source-post ::read-source-post)

(def parse-file-pre ::parse-file-pre)
(def parse-file ::parse-file)
(def parse-file-post ::parse-file-post)

(def parse-content-pre ::parse-content-pre)
(def parse-content ::parse-content)
(def parse-content-post ::parse-content-post)

(def write-output-pre ::write-output-pre)
(def write-output ::write-output)
(def write-output-post ::write-output-post)

(def generate-routes-pre ::generate-routes-pre)
(def generate-routes ::generate-routes)
(def generate-routes-post ::generate-routes-post)

(def shutdown-cli-pre ::shutdown-cli-pre)
(def shutdown-cli ::shutdown-cli)
(def shutdown-cli-post ::shutdown-cli-post)

(def file-change ::file-change)
(def file-create ::file-create)
(def file-modify ::file-modify)
(def file-delete ::file-delete)

(def content-regen ::content-regen)
(def css-regen ::css-regen)
(def system-restart ::system-restart)
