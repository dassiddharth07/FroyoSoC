## SoC Hardware

The SoC consists of a 32-bit VexRISC-V CPU and the FROST accelerator tightly coupled using the CFU interface.
To generate the SoC Verilog including RISC-V CPU and FROST accelerator, first install the sbt Scala build tool. The top-level SoC can be generated with the following command:

sbt "runMain vexriscv.ip.modrec.ModRecSoc"

## Testbench Simulation

There are cocotb testbenches to perform RTL simulation of the software benchmarks executing on the SoC. First source the cocotb_env.sh script to setup the cocotb environment. Then execute the Makefile in Froyo_test/ via 'make' to run the simulation. 