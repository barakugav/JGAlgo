# the makefile is a generic makefile which use the following parameters:
#       INCLUDES		list of all folders containing the includes (format: -Ia/include/ -Ib/)
#       SOURCES		 list of all source files (format: a.c b.c t.S)
#       DEFINES		 defines passed to the compiled files (format: -DPARAM_A -DPARAM_B)
#       OUTPUT_DIR	      path to output directory, use for objs
#       OUT_EXE		 path to output executable
#       COMPIER		 compiler
#       COMPILER_FLAGS	  flags for the compiler
#       LINK_FLAGS	      flags for linking library
#       GCC_ARCH_CFLAGS	 arch flags
#       ADDITIONAL_PREREQUISITES	list of additional prerequisites for the build taget

CC := $(COMPILER)
CC := $(shell which $(CC) 2>/dev/null || type -p $(CC) || echo $(CC))

# Check GCC compiler for compile flag support
#
# @param compile flag
# @return provided compile flag if supported by compiler, empty otherwise
define gcc_check_flag
$(shell $(CC) $1 -E - < /dev/null > /dev/null 2>&1 && echo $1)
endef

# Notice: make sure these execute only once and not recursively
# GCC_ARCH_CFLAGS += $(call gcc_check_flag,-marm)
# GCC_ARCH_CFLAGS += $(call gcc_check_flag,-mfpu=neon-vfpv4)
ARCH_CFLAGS += $(GCC_ARCH_CFLAGS)

#We sort CFLAGS because sort removes duplicates string
CFLAGS = $(sort -MD -MP $(INCLUDES) $(DEFINES) $(ARCH_CFLAGS) $(COMPILER_FLAGS))
ARFLAGS = -rcs
LDFLAGS = $(LINK_FLAGS)

SOURCES_C = $(filter %.c,$(SOURCES))
# SOURCES_S = $(filter %.S,$(SOURCES))
OBJS_C = $(patsubst %.c,$(OUTPUT_DIR)/%.o,$(SOURCES_C))
# OBJS_S = $(patsubst %.S,$(OUTPUT_DIR)/%.o,$(SOURCES_S))
# OBJS = $(OBJS_C) + $(OBJS_S)
OBJS = $(OBJS_C)

###############################################################
#
# Verbose
#
###############################################################

ifeq ("$(origin V)", "command line")
VERBOSE := $(V)
else
VERBOSE = 0
endif

ifeq ($(VERBOSE),1)
quiet =
else
quiet = quiet_
endif

# make -s should not create any output
ifneq ($(filter s% -s%,$(MAKEFLAGS)),)
quiet = silent_
endif

squote := '
escsq = $(subst $(squote),'\$(squote)',$1)

echo-cmd = $(if $($(quiet)cmd_$(1)),\
	echo '  $(call escsq,$($(quiet)cmd_$(1)))';)

###############################################################
#
# Commands
#
###############################################################

# Check if the invocation of make was with -B/--always-make
# this will force all special target to be rebuild
ifneq ($(filter -%B,$(MAKEFLAGS)),)
FORCE_BUILD := 1
else
FORCE_BUILD :=
endif

# Find any prerequisites that is newer than target
# Find any prerequisites that does not exists
# Find if the target itself doesn't exists
check_prereq = \
	$(filter-out FORCE,$?) \
	$(filter-out FORCE $(wildcard $^),$^) \
	$(filter-out FORCE $(wildcard $@),$@) \
	$(FORCE_BUILD)

# Check if the command line changed from previous build
check_cmd = \
	$(filter-out $(cmd_$@),$(cmd_$(1))) \
	$(filter-out $(cmd_$(1)),$(cmd_$@))

# Replace >$< with >$$< to preserve $ when reloading the .cmd file
# (needed for make)
# Replace >#< with >\#< to avoid starting a comment in the .cmd file
# (needed for make)
# Replace >'< with >'\''< to be able to enclose the whole string in '...'
# (needed for the shell
make_cmd = $(subst \#,\\\#,$(subst $$,$$$$,$(call escsq,$(cmd_$(1)))))

# saved command line file name
cmd_file_name = $(dir $(1)).$(notdir $(1)).cmd

# Makefile include files
include_cmd_files = $(wildcard $(foreach f,$(1),$(call cmd_file_name,$(f))))

target_rule = \
	$(if $(strip $(check_cmd) $(check_prereq)),\
		@set -e; \
		mkdir -p $(@D); \
		echo 'cmd_$@ := $(make_cmd)' > $(call cmd_file_name,$@); \
		$(echo-cmd) \
		$(cmd_$(1)))

cc_o_c_rule = $(call target_rule,cc_o_c)
ifeq ($(filter %sparse,$(CC)),)
quiet_cmd_cc_o_c = CC      $@
else
quiet_cmd_cc_o_c = CHK     $@
endif
cmd_cc_o_c = $(CC) $(CFLAGS) -c -o $@ $<

cc_o_s_rule = $(call target_rule,cc_o_s)
ifeq ($(filter %sparse,$(CC)),)
quiet_cmd_cc_o_s = AS      $@
else
quiet_cmd_cc_o_c = CHK     $@
endif
cmd_cc_o_c = $(CC) $(CFLAGS) -c -o $@ $<

cc_o_s_rule = $(call target_rule,cc_o_s)
ifeq ($(filter %sparse,$(CC)),)
quiet_cmd_cc_o_s = AS      $@
else
quiet_cmd_cc_o_s = CHK     $@
endif
cmd_cc_o_s = $(CC) $(CFLAGS) -c -o $@ $<

ld_rule = $(if $(filter %sparse,$(CC)),,$(call target_rule,ld))
quiet_cmd_ld = LD      $@
cmd_ld = $(CC) $(filter-out FORCE,$^) -o $@ $(LDFLAGS)

ar_rule = $(if $(filter %sparse,$(CC)),,$(call target_rule,ar))
quiet_cmd_ar = AR      $@
cmd_ar = $(AR) $(ARFLAGS) $@ $(filter-out FORCE,$^)

###############################################################
#
# Targets
#
###############################################################

.PHONY: all clean FORCE
.PRECIOUS: $(OBJS)

all: $(ADDITIONAL_PREREQUISITES) $(OUT_EXE)

$(OUT_EXE): $(OBJS) FORCE
	$(call ld_rule)

$(OUTPUT_DIR)/%.o: %.c
	$(call cc_o_c_rule)

FORCE:

clean:
	rm -rf $(OUTPUT_DIR)

