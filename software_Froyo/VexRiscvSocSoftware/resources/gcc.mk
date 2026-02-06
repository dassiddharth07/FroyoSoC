# Set it to yes if you are using the sifive precompiled GCC pack
SIFIVE_GCC_PACK ?= yes

ifeq ($(SIFIVE_GCC_PACK),yes)
	RISCV_NAME ?= riscv64-unknown-elf
	RISCV_PATH ?= <Path to --> sifive_toolchain/riscv64-unknown-elf-gcc-8.3.0-2019.08.0-x86_64-linux-ubuntu14
else
	RISCV_NAME ?= riscv32-unknown-elf
	ifeq ($(MULDIV),yes)
		RISCV_PATH ?= /opt/riscv32im/
	else
		RISCV_PATH ?= /opt/riscv32i/
	endif
endif

MABI=ilp32
MARCH := rv32i
ifeq ($(MULDIV),yes)
	MARCH := $(MARCH)m
endif
ifeq ($(COMPRESSED),yes)
	MARCH := $(MARCH)ac
endif
ifeq ($(FLOAT),yes)
	MARCH := $(MARCH)f
	MABI := $(MABI)f
endif
ifeq ($(COMPRESSED),yes)
	MARCH := $(MARCH)c
endif

CFLAGS += -march=$(MARCH)  -mabi=$(MABI)
LDFLAGS += -march=$(MARCH)  -mabi=$(MABI)
