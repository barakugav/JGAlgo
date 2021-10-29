
ifndef ALGO_SRC_MAIN_C_BASE
mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
# current_dir := $(notdir $(patsubst %/,%,$(dir $(mkfile_path))))
current_dir := $(patsubst %/,%,$(dir $(mkfile_path)))
ALGO_SRC_MAIN_C_BASE = $(current_dir)
endif

ALGO_SOURCES = \
	$(ALGO_SRC_MAIN_C_BASE)/src/rmq.c \

