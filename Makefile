PROJ = dragon
ROOT_DIR = $(shell pwd)
export PATH := $(PATH):$(ROOT_DIR)/bin
REPO = $(shell git config --get remote.origin.url)

include dev-resources/make/code.mk
include dev-resources/make/docs.mk
include dev-resources/make/git.mk
include dev-resources/make/test.mk
