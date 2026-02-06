/*
 * Copyright 2021 The CFU-Playground Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef CFU_H
#define CFU_H

#include <stdint.h>

#include "riscv.h"
#include "software_cfu.h"

/* riscv.h defines a macro:

    #define opcode_R(opcode, funct3, funct7, rs1, rs2)

   that returns at 32b value.  The opcode must be "CUSTOM0" (also defined in
   riscv.h).

   'func3' is used as functionID sent to the CFU.


*/

// =============== Access the custom instruction

#define CFU_STATE_INDEX_CSR_ADDR 0xBC0

// generic name for each custom instruction - via hardware
#define cfu_op_hw(funct3, funct7, rs1, rs2) \
  opcode_R(CUSTOM0, funct3, funct7, (rs1), (rs2))
#define cfu_op0_hw(funct7, rs1, rs2) cfu_op_hw(0, funct7, rs1, rs2)
#define cfu_op1_hw(funct7, rs1, rs2) cfu_op_hw(1, funct7, rs1, rs2)
#define cfu_op2_hw(funct7, rs1, rs2) cfu_op_hw(2, funct7, rs1, rs2)
#define cfu_op3_hw(funct7, rs1, rs2) cfu_op_hw(3, funct7, rs1, rs2)
#define cfu_op4_hw(funct7, rs1, rs2) cfu_op_hw(4, funct7, rs1, rs2)
#define cfu_op5_hw(funct7, rs1, rs2) cfu_op_hw(5, funct7, rs1, rs2)
#define cfu_op6_hw(funct7, rs1, rs2) cfu_op_hw(6, funct7, rs1, rs2)
#define cfu_op7_hw(funct7, rs1, rs2) cfu_op_hw(7, funct7, rs1, rs2)

// generic name for each custom instruction - via software
#define cfu_op_sw(funct3, funct7, rs1, rs2) \
  software_cfu(funct3, funct7, rs1, rs2)
#define cfu_op0_sw(funct7, rs1, rs2) cfu_op_sw(0, funct7, rs1, rs2)
#define cfu_op1_sw(funct7, rs1, rs2) cfu_op_sw(1, funct7, rs1, rs2)
#define cfu_op2_sw(funct7, rs1, rs2) cfu_op_sw(2, funct7, rs1, rs2)
#define cfu_op3_sw(funct7, rs1, rs2) cfu_op_sw(3, funct7, rs1, rs2)
#define cfu_op4_sw(funct7, rs1, rs2) cfu_op_sw(4, funct7, rs1, rs2)
#define cfu_op5_sw(funct7, rs1, rs2) cfu_op_sw(5, funct7, rs1, rs2)
#define cfu_op6_sw(funct7, rs1, rs2) cfu_op_sw(6, funct7, rs1, rs2)
#define cfu_op7_sw(funct7, rs1, rs2) cfu_op_sw(7, funct7, rs1, rs2)

// generic name for each custom instruction - switchable
#define cfu_op0(funct7, rs1, rs2) cfu_op(0, funct7, rs1, rs2)
#define cfu_op1(funct7, rs1, rs2) cfu_op(1, funct7, rs1, rs2)
#define cfu_op2(funct7, rs1, rs2) cfu_op(2, funct7, rs1, rs2)
#define cfu_op3(funct7, rs1, rs2) cfu_op(3, funct7, rs1, rs2)
#define cfu_op4(funct7, rs1, rs2) cfu_op(4, funct7, rs1, rs2)
#define cfu_op5(funct7, rs1, rs2) cfu_op(5, funct7, rs1, rs2)
#define cfu_op6(funct7, rs1, rs2) cfu_op(6, funct7, rs1, rs2)
#define cfu_op7(funct7, rs1, rs2) cfu_op(7, funct7, rs1, rs2)

// =============== Switch HW vs SW

#ifdef CFU_SOFTWARE_DEFINED
#define cfu_op(funct3, funct7, rs1, rs2) cfu_op_sw(funct3, funct7, rs1, rs2)
#else
#define cfu_op(funct3, funct7, rs1, rs2) cfu_op_hw(funct3, funct7, rs1, rs2)
#endif

#endif  // CFU_H

#define set_cfu_state_id(state, id) \
    csr_set(0xBC0, 0x00000000 | (state)<<16 | id)

#define clear_cfu_state_id(state, id) \
    csr_clear(0xBC0, 0x00000000 | (state)<<16 | id)

// CFU Manager CSR
#define SSCA_CTRL_SIGNALS 0 //0:sync_mode, 1:bramAccessEn, 2:confMemAccessEn, 3:arst, 4:loopbreak, 5:gndBlkOuts, 6:startConfig, (26,16):confStartAddress
#define FPGA_CTRL_IO 1
#define CLKGEN_0 2 //31:clkgen_select, 30:fastclksel, 29:reset, (11,0):clkgenconfig(11:0)
#define CLKGEN_1 3 //(24, 0): clkconfig(36:12)
#define CLKGEN_2 4 //clkconfig(68:37)
#define CLKGEN_3 5
#define DMA_RD_ADDR 6
#define DMA_WR_ADDR 7
#define DMA_CTRL 8 // Msb is fire job, then [23:0] lsbs are word_count
#define FROST_CTRL_SIGNALS 9
#define SHA_CTRL_SIGNALS_0 10
#define SHA_CTRL_SIGNALS_1 11

#define SSCA_STATUS_IO 0 
#define DMA_STATUS 1 // lsb is job_done
#define FPGA_CONF_STATUS 2
#define FROST_STATUS_IO 3
#define SHA_STATUS_IO 4

#define cfu_csr_read(reg_addr)        cfu_op(0, reg_addr, 0, 0)
#define cfu_csr_write(reg_addr, val)  cfu_op(1, reg_addr, val, 0)

#define cfu_select_manager  clear_cfu_state_id(0, 1)
#define cfu_select_ssca     set_cfu_state_id(0, 1)

#define cfu_dmem_ctrl_sha  cfu_csr_write(SSCA_CTRL_SIGNALS, 0x00000002);
#define cfu_pmem_ctrl_ssca  cfu_csr_write(SSCA_CTRL_SIGNALS, 0x00000004);
#define cfu_dpmem_ctrl_ssca cfu_csr_write(SSCA_CTRL_SIGNALS, 0x0000000A);
#define cfu_mem_ctrl_cpu    cfu_csr_write(SSCA_CTRL_SIGNALS, 0x00000000);

#define cfu_dmem_ctrl_frost  cfu_csr_write(SSCA_CTRL_SIGNALS, 0x00000006);
