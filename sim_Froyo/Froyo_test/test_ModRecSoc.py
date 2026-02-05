import cocotb
from cocotb.triggers import Timer,ClockCycles
from cocotb.result import TestFailure, TestSuccess
from cocotb.binary import BinaryValue
from cocotb.clock import Clock
from cocotb.utils import get_sim_time
from cocotb.triggers import Edge,FallingEdge,ClockCycles
from cocotb.handle import HierarchyObject, Force

import numpy as np
import random, copy, sys


from collections import deque

step = cocotb.plusargs['STEP']
_ns = 1000
T = 1*_ns

SW_ROOT = cocotb.plusargs['SW_PATH']
SW_TEST = "test_scalarmult_all"
TEST_PATH = SW_ROOT + SW_TEST + "/build/" + SW_TEST + ".hex"

@cocotb.test()
def ModRecSoc_dma_test(d):
    global dut, io, chains
    dut = d
        
    yield Timer(1*_ns)

    reset_signals()

    if 'syn' in step:
        # dut.resetCtrl_systemResetCounter_reg[0]
        for i in range(6):
            getattr(dut, f"\\resetCtrl_systemResetCounter_reg[{i}] .o").value = 0

        for i in range(8):
            getattr(dut, f"systemDebugger_1.\\dispatcher_headerShifter_reg[{i}] .o1").value = 0

    


    with open(TEST_PATH, "r") as f:
        lines = f.readlines()
        detail_hex = []
        # print(lines, file=f2)
        for line in lines:
            if line[7:9] == '00':
                detail_hex.extend(line.split())
            else:
                continue
        # print(detail_hex, file=f2)

    addr = 0
    numColBits = 10
    numRowBits = 3
    for hex_str in detail_hex:
        data = []
        length = int(hex_str[1:3], 16)*2
        addr = int(int(hex_str[3:7], 16)/4)
        for i in range(0, length, 8):
            cnt = int(((addr+(i/8))/4096))
            addr_mem = int(((addr+(i/8))%4096))
            val = hex_str[9+i:9+i+8]
            val_rev = val[6:8]+val[4:6]+val[2:4]+val[0:2]

            macroNum = addr // 8192
            rowAddr = (addr >> numRowBits) & ((1 << numColBits) - 1)
            colAddr = addr & ((1 << numRowBits) - 1)

            if 'rtl' in step:
                getattr(dut, f"axi_ram.ram.sram_macros_0.MEMORY[{addr}]").value = int(val_rev, 16)        
            else:
                getattr(dut, f"axi_ram.ram.sram_macros_0.MEMORY[{addr}]").value = int(val_rev, 16)
            addr += 1

        

    # Start clock and reset
    cocotb.start_soon(Clock(dut.io_axiClk,1.1,'ns').start())
    cocotb.start_soon(Clock(dut.io_accelClk,1,'ns').start())
    dut.io_asyncReset.value = 1
    dut.io_accelReset.value = 1
    yield ClockCycles(dut.io_axiClk,200)
    dut.io_asyncReset.value = 0
    dut.io_accelReset.value = 0

    print("here2")


    # Wait & end sim
    yield ClockCycles(dut.io_axiClk,40000)

    print("\n\n\n STDOUT \n\n\n")
    print_ocmem(32)

def get_ocmem_vals(numwords):
    vals = []
    numColBits = 10
    numRowBits = 3
    for addr in range(numwords):
        rowAddr = (addr >> numRowBits) & ((1 << numColBits) - 1)
        colAddr = addr & ((1 << numRowBits) - 1)
        read_out = getattr(dut, f"axi_ram.ram.sram_macros_4.MEMORY[{addr}]").value.binstr
        word = hex(int(read_out.replace('x','0'),2))[2:].zfill(8)
        vals.append(word)
    return vals

def print_ocmem(offset=0x8000, size=200):
    read_string = get_ocmem_vals(size)
    
    for i in read_string:
        #print(i)
        x = bytearray.fromhex(i).decode()[::-1]
        for i in ("").join(x):    
            if (ord(i) == 10):
                print("")
            if (ord(i) >= 32 and ord(i) <= 127):
                print(i, end='')

def reset_signals():
    dut.io_asyncReset.value = 0
    dut.io_axiClk.value = 0
    dut.io_jtag_tms.value = 0
    dut.io_jtag_tdi.value = 0
    dut.io_jtag_tck.value = 0
    dut.io_gpioA_read.value = 0
    dut.io_gpioB_read.value = 0
    dut.io_uart_rxd.value = 0
    dut.io_coreInterrupt.value = 0
    dut.io_testMode.value = 0
    dut.io_scan_dataIn_valid.value = 0
    dut.io_scan_dataIn_payload.value = 1
    dut.io_scan_load.value = 0
    dut.io_scan_read.value = 0
    dut.io_scan_reset.value = 1
    dut.io_scan_clock.value = 0
    dut.io_chainSelEn.value = 0






