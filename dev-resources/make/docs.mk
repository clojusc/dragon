DOCS_DIR = $(ROOT_DIR)/docs
CURRENT = $(DOCS_DIR)/current
LOCAL_DOCS_HOST = localhost
LOCAL_DOCS_PORT = $(lastword $(shell grep port project.clj))

docs:
	lein with-profile +docs codox

docs-run: docs
	@echo
	@echo "###   Running docs server on http://$(LOCAL_DOCS_HOST):$(LOCAL_DOCS_PORT) ... "
	@echo
	@lein with-profile +docs simpleton $(LOCAL_DOCS_PORT) file :from $(CURRENT)

.PHONY: docs
