## Tool versions & dependencies

* Compile scala
    * [sbt] (https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html)

* Software compilation
    * [sifive_toolchain] (Instructions in software_Froyo/VexRiscvSoftware/README.md)

* Verification
    * [cocotb](https://github.com/cocotb/cocotb): 1.8.1
    * [pytest](https://docs.pytest.org/en/8.0.x/): 7.0.1
    * Cadence Xcelium: 22.03

## Instructions

### STEP 1: Create the SoC RTL

The SoC consists of a 32-bit VexRISC-V CPU and the FROST accelerator tightly coupled using the CFU interface.
To generate the SoC Verilog including RISC-V CPU and FROST accelerator, first install the sbt Scala build tool. The top-level SoC can be generated with the following command:

```command
cd VexRiscv_Froyo
sbt "runMain vexriscv.ip.modrec.ModRecSoc"
```

The RTL file should be in `VexRiscv_Froyo/verilog_outputs`.

### STEP 2: Compile the C program
Make sure you install the gcc toolchain and point to its location in `software_Froyo/VexRiscvSoftware/resources/gcc.mk`

```command
cd software_Froyo/VexRiscvSoftware/projects/modrec/test_Froyo
make
```
This should dump the hex, elf and v files in `software_Froyo/VexRiscvSoftware/projects/modrec/test_Froyo/build` directory.

You can change what operations you want to run in `software_Froyo/VexRiscvSoftware/projects/modrec/test_Froyo/src/main.c`. We provide a list of c functions in the other files in src/. Make sure you change the location from which values are printed. (Default set to 8)

### STEP 2: Test the program execution on SoC

Make sure you install cocotb, pytest and create the virtual python environment. Provide path to xcelium in the `sim_Froyo/cocotb_env.sh` file.

```command
cd sim_Froyo/Froyo_test
source ../cocotb_env.sh
make
```

The terminal should print the values you need the software to print. You can also open the waveform for the program execution.

```command
simvision dump_new.shm &
```

You can control the frequency of clocks and time for simulation run in `test_ModRecSoc.py`.
