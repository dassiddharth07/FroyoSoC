######################################################
#	Cocotb settings
######################################################
TOPLEVEL_LANG ?= verilog
PWD=$(shell pwd)
SIM ?= xcelium
COCOTB_HDL_TIMEUNIT ?= 1ps
COCOTB_HDL_TIMEPRECISION ?= 1ps

ifeq ($(OS),Msys)
WPWD=$(shell sh -c 'pwd -W')
PYTHONPATH := $(WPWD)/../model;$(PYTHONPATH)
else
WPWD=$(shell pwd)
PYTHONPATH := $(WPWD)/../model:$(PYTHONPATH)
endif

######################################################
#	Testbench settings
######################################################
STEP 	 ?= rtl_full
TOPLEVEL := ModRecSoc
MODULE   := test_ModRecSoc
SHM ?= 1
SW_ROOT = ../software/VexRiscvSocSoftware/projects/modrec/
PLUSARGS += "+STEP=$(STEP)" \
			"+SW_PATH=$(SW_ROOT)"


######################################################
#	RTL simulation sources
######################################################
SOC_VERILOG_PATH = ../VexRiscv_Froyo/verilog_outputs
SHA_VERILOG_PATH = ../VexRiscv_Froyo/verilog_outputs/sha_verilog


ifeq ($(STEP),rtl_full)

VERILOG_SOURCES += $(SOC_VERILOG_PATH)/ModRecSoc.v # VexRV SoC
VERILOG_SOURCES += $(SOC_VERILOG_PATH)/Ram_1w1rs.sv
VERILOG_SOURCES += $(SOC_VERILOG_PATH)/Mem_macros.sv
VERILOG_SOURCES += $(SHA_VERILOG_PATH)/*.v


else ifeq ($(STEP),chip)
VERILOG_SOURCES += 
TOPLEVEL := chip_top_ring

else ifeq ($(STEP),syn)
VERILOG_SOURCES +=
COMPILE_ARGS += 

######################################################
#	Post-apr simulation source files
######################################################
else ifeq ($(STEP),apr)


else ifeq ($(STEP),chip_apr)

VERILOG_SOURCES += 

TOPLEVEL := chip_top_ring 
COMPILE_ARGS+="-add_seq_delay 10ps"
COMPILE_ARGS+="-define POWER_AWARE_MODE"


endif

######################################################
#	Common simulation source files
######################################################


######################################################
#	Simulator flags/settings
######################################################
# Common simulator settings
COMPILE_ARGS+="-sv"
COMPILE_ARGS+="-define RANDOMIZE_REG_INIT"
COMPILE_ARGS+="-define RANDOMIZE_MEM_INIT"
COMPILE_ARGS+="-define PRINTF_COND=1"
COMPILE_ARGS+="-define FORMAL"

# Simulator-specific settings
ifeq ($(SIM),questa)
COMPILE_ARGS+="+acc=rc +libext+.v +libext+.sv"
COMPILE_ARGS+="-suppress vlog-12003"
SIM_ARGS+="-suppress vlog-12003"

else ifeq ($(SIM),xcelium)
COMPILE_ARGS+="-nowarn PRPASZ" # SHM can't probe wide signals warning
COMPILE_ARGS+="-nowarn LMTMSG" # SHM can't probe wide signals warning 2


# COMPILE_ARGS+="-gateloopwarn"
# COMPILE_ARGS+="-access +rwc"
# COMPILE_ARGS+="-notimingchecks"
# COMPILE_ARGS+="-warn_multiple_driver"
# COMPILE_ARGS+="-ALLOWREDEFINITION"
# COMPILE_ARGS+="-vlog_ext +.vg"
# COMPILE_ARGS+="-libext +.sv"
# COMPILE_ARGS+="-libext +.v"

ifeq ($(SHM),1)
COMPILE_ARGS+=-input shmdump.tcl
endif

endif

include $(shell cocotb-config --makefiles)/Makefile.sim
