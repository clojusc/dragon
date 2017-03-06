PROJ = dragon
ROOT_DIR = $(shell pwd)
export PATH := $(PATH):$(ROOT_DIR)/bin

include dev-resources/make/publish.mk
include dev-resources/make/setup.mk
include dev-resources/make/code.mk
include dev-resources/make/test.mk
include dev-resources/make/git.mk
