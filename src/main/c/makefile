ifndef ALGO_TOP
$(error ALGO_TOP is not define)
endif

include pre_makefile.mk

ifndef ALGO_SRC_MAIN_C_BASE
mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
# current_dir := $(notdir $(patsubst %/,%,$(dir $(mkfile_path))))
current_dir := $(patsubst %/,%,$(dir $(mkfile_path)))
ALGO_SRC_MAIN_C_BASE = $(current_dir)
endif

include $(ALGO_SRC_MAIN_C_BASE)/filelist.mk

# the makefile is a generic makefile which use the following parameters:
#       INCLUDES                list of all folders containing the includes (format: -Ia/include/ -Ib/)
#       SOURCES          list of all source files (format: a.c b.c t.S)
#       DEFINES          defines passed to the compiled files (format: -DPARAM_A -DPARAM_B)
#       OUTPUT_DIR            path to output directory, use for objs
#       OUT_EXE          path to output executable
#       COMPIER          compiler
#       COMPILER_FLAGS    flags for the compiler
#       LINK_FLAGS            flags for linking library
#       GCC_ARCH_CFLAGS  arch flags
#       ADDITIONAL_PREREQUISITES        list of additional prerequisites for the build taget

INCLUDES = \
	-I$(ALGO_SRC_MAIN_C_BASE)/inc/ \

SOURCES = \
	$(ALGO_SOURCES) \

OUTPUT_DIR = $(ALGO_TOP)/build
OUT_EXE = $(OUTPUT_DIR)/algo
COMPILER = /usr/bin/gcc

include post_makefile.mk

