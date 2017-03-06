DOCS_DIR = $(ROOT_DIR)/docs
CSS_DIR = $(DOCS_DIR)/assets/css
REPO = $(shell git config --get remote.origin.url)
LOCAL_DOCS_HOST = localhost
LOCAL_DOCS_PORT = $(lastword $(shell grep dev-port project.clj))
LESS_DIR = src/less
COLOUR_THEME = clojang

blog: blog-clean blog-local

blog-clean:
	@echo "\nCleaning old blog build ..."

blog-pre:
	@echo "\nBuilding blog ..."

blog-css:
	@echo "\nGenerating CSS with LESS ..."
	@lessc $(LESS_DIR)/$(COLOUR_THEME).less $(CSS_DIR)/styles.css
	@lessc $(LESS_DIR)/blog.less $(CSS_DIR)/blog.css

blog-clojure:
	@blog gen
	@echo

blog-local: blog-pre blog-css blog-clojure

blog-dev-gen: blog
	@echo
	@echo "\nRunning blog server from generated static content ..."
	@echo "URL: http://$(LOCAL_DOCS_HOST):$(LOCAL_DOCS_PORT)"
	@lein simpleton $(LOCAL_DOCS_PORT) file :from $(DOCS_DIR)

blog-dev:
	@echo
	@echo "\nRunning blog server from code ..."
	@echo "URL: http://$(LOCAL_DOCS_HOST):$(LOCAL_DOCS_PORT)"
	@blog run

.PHONY: blog
