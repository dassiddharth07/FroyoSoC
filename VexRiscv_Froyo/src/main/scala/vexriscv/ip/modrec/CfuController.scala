package vexriscv.ip.modrec

import spinal.core
import spinal.core._
import spinal.lib._
import vexriscv.plugin.{CfuBus, CfuBusParameter}
import spinal.lib.bus.simple._
import vexriscv.ip.modrec.ModRecSocConfig

// Status registers
object StatusRegAddr extends SpinalEnum {
  val SSCA_STATUS, DMA_STATUS, DL_STATUS, FROST_STATUS, SHA_STATUS = newElement()

  defaultEncoding = SpinalEnumEncoding("staticEncoding")(
    SSCA_STATUS -> 0,
    DMA_STATUS -> 1,
    DL_STATUS -> 2,
    FROST_STATUS -> 3,
    SHA_STATUS -> 4
  )
}

// Control registers
object CtrlRegAddr extends SpinalEnum {
  val SSCA_CTRL_SIGNALS,
      FPGA_CTRL_IO,
      CLKGEN_0,
      CLKGEN_1,
      CLKGEN_2,
      CLKGEN_3,
      DMA_RD_ADDR,
      DMA_WR_ADDR,
      DMA_CTRL,
      FROST_CTRL_SIGNALS,
      SHA_CTRL_SIGNALS_0,
      SHA_CTRL_SIGNALS_1 = newElement()

  defaultEncoding = SpinalEnumEncoding("staticEncoding")(
    SSCA_CTRL_SIGNALS -> 0,
    FPGA_CTRL_IO -> 1,
    CLKGEN_0 -> 2,
    CLKGEN_1 -> 3,
    CLKGEN_2 -> 4,
    CLKGEN_3 -> 5,
    DMA_RD_ADDR -> 6,
    DMA_WR_ADDR -> 7,
    DMA_CTRL -> 8,
    FROST_CTRL_SIGNALS -> 9,
    SHA_CTRL_SIGNALS_0 -> 10,
    SHA_CTRL_SIGNALS_1 -> 11
  )
}

import CtrlRegAddr._
import StatusRegAddr._

case class CfuController(planMemWordCount: BigInt) extends Component {

  val io = new Bundle {
    val cfu = slave(CfuBus(ModRecSocConfig.default.cfuBusParam))

    // Manager IOs
    // FPGA
//    val fpgaCtrl = new Bundle {
//      val ctrlIO = out Bits(32 bits)
//      val statusIO = in Bits(32 bits)
//      val cfuSyncMode = out Bool()
//      val confMemAccessEn = out Bool()
//      val bramFpgaAccessEn = out Bool()
//
//      val arst = out Bool()
//      val loopBreak = out Bool()
//      val gndBlkOuts = out Bool()
//      val latchEn = out Bool()
//      val startConfig = out Bool()
//      val doneConfig = in Bool()
//      val confStartAddr = out UInt(log2Up(2048) bits)
//    }

    // FFT Accel
    val sscaAccelCtrl = new Bundle {
      val cfuSyncMode = out Bool()
      val planMemAccessEn = out Bool()
      val dataMemAccessEn = out Bits(2 bits)


      val arst        = out Bool()
      val start = out Bool()
      val done  = in Bool()
      val planStartAddr = out UInt(log2Up(planMemWordCount) bits)
      val planStartAddr1 = out UInt(log2Up(planMemWordCount) bits)
    }
    
    // DMA
    val dmaCtrl = new Bundle {
      val irq = in Bool()
      val fireJob = out Bool()
      val jobDone = in Bool()
      val readAddr = out UInt(32 bits)
      val writeAddr = out UInt(32 bits)
      val wordCount = out UInt(24 bits)
    }
    
    // Clkgen
    val clkGenConfig = out Bits(69 bits)
    val clkGenSelect = out Bool()
    val clkGenfastClockSel = out Bool()
    val clkGenReset = out Bool()

    // Frost
    val frostCtrl = new Bundle {
      val start = out Bool()
      val ready = out Bool()
      val done = in Bool()

      val mode = out Bits(4 bits)
      val offsetRd = out Bits(8 bits)
      val offsetWr = out Bits(8 bits)
    }

    // SHA
    val shaCtrl = new Bundle {
      val start = out Bool()
      val ready = out Bool()
      val mode  = out Bits(2 bits)
      val no_update  = out Bool()

      val rdOffset = out UInt(8 bits)
      val wrOffset = out UInt(8 bits)

      val work_factor = out Bool()
      val work_factor_num = out Bits(32 bits)

      val done  = in Bool()
    }

  }

  // CFU Inst encoding (ID = 0)
  // op (func3 = 0) - CSR read (func7 - control reg addr)
  // op (func3 = 1) - CSR write (func7 - control reg addr)
  // op2 - waitOn (trigger clkgen, DMA) ?? polling Status
  val funct7 = io.cfu.cmd.payload.raw_insn(31 downto 25)
  val funct3 = io.cfu.cmd.payload.raw_insn(14 downto 12)


  // CTRL registers
  val ctrlRegInits = List(
    B("32'h0000_0080"), // reg0 - SSCA_CTRL_SIGNALS
    B("32'h0000_0000"), // reg1 - FPGA_CTRL_IO
    B("32'b11_0000_111111"), // reg2 - CLKGEN_0
    B("32'b0000000000000000000000001"), // reg3 - CLKGEN_1
    B("32'b0_100_100_100_100_0_100_100_100_100_000_000"), // reg4 - CLKGEN_2
    B("32'h0000_0000"), // reg5 - CLKGEN_3
    B("32'h0000_0000"), // reg6 - DMA_RD_ADDR
    B("32'h0000_0000"), // reg7 - DMA_WR_ADDR
    B("32'h0000_0000"), // reg8 - DMA_CTRL
    B("32'h0000_0000"), // reg9 - FROST_CTRL_SIGNALS
    B("32'h0000_0000"), // reg10 - SHA_CTRL_SIGNALS_0
    B("32'h000F_0000")  // reg11 - SHA_CTRL_SIGNALS_1
  )

  val ctrlRegs = Vec(ctrlRegInits.zipWithIndex.map { case (init, id) =>
    RegNextWhen(io.cfu.cmd.payload.inputs(0),
      io.cfu.cmd.fire && (funct7 === id) && (funct3 === 1),
      init)
  })


  // SSCA Accel
  io.sscaAccelCtrl.cfuSyncMode := ctrlRegs(SSCA_CTRL_SIGNALS.asBits.asUInt.resized)(0).addTag(crossClockDomain)
//  // lower bit selects between cpu (X0) and accels (X1). upper bit selects between frost (11) /ssca (01)
  io.sscaAccelCtrl.dataMemAccessEn := ctrlRegs(SSCA_CTRL_SIGNALS.asBits.asUInt.resized)(2 downto 1)
  io.sscaAccelCtrl.planMemAccessEn := ctrlRegs(SSCA_CTRL_SIGNALS.asBits.asUInt.resized)(3)
  io.sscaAccelCtrl.arst := ctrlRegs(SSCA_CTRL_SIGNALS.asBits.asUInt.resized)(4)
////  io.fpgaCtrl.loopBreak := ctrlRegs(FPGA_CTRL_SIGNALS.asBits.asUInt.resized)(4)
////  io.fpgaCtrl.gndBlkOuts := ctrlRegs(FPGA_CTRL_SIGNALS.asBits.asUInt.resized)(5)
  io.sscaAccelCtrl.start := ctrlRegs(SSCA_CTRL_SIGNALS.asBits.asUInt.resized)(5)
////  io.fpgaCtrl.latchEn := ctrlRegs(FPGA_CTRL_SIGNALS.asBits.asUInt.resized)(7)
  io.sscaAccelCtrl.planStartAddr := ctrlRegs(SSCA_CTRL_SIGNALS.asBits.asUInt.resized)((log2Up(planMemWordCount)+5) downto 6).asUInt
  io.sscaAccelCtrl.planStartAddr1 := ctrlRegs(SSCA_CTRL_SIGNALS.asBits.asUInt.resized)((2*log2Up(planMemWordCount)+5) downto (log2Up(planMemWordCount)+6)).asUInt

  // CLKgen
  io.clkGenConfig(11 downto 0) := ctrlRegs(CLKGEN_0.asBits.asUInt.resized)(11 downto 0).addTag(crossClockDomain)
  io.clkGenSelect := ctrlRegs(CLKGEN_0.asBits.asUInt.resized)(31)
  io.clkGenfastClockSel := ctrlRegs(CLKGEN_0.asBits.asUInt.resized)(30)
  io.clkGenReset := ctrlRegs(CLKGEN_0.asBits.asUInt.resized)(29)
  io.clkGenConfig(36 downto 12) := ctrlRegs(CLKGEN_1.asBits.asUInt.resized)(24 downto 0).addTag(crossClockDomain)
  io.clkGenConfig(68 downto 37) := ctrlRegs(CLKGEN_2.asBits.asUInt.resized)(31 downto 0).addTag(crossClockDomain)

  // DMA
  io.dmaCtrl.readAddr :=  ctrlRegs(DMA_RD_ADDR.asBits.asUInt.resized).asUInt
  io.dmaCtrl.writeAddr :=  ctrlRegs(DMA_WR_ADDR.asBits.asUInt.resized).asUInt
  io.dmaCtrl.wordCount :=  ctrlRegs(DMA_CTRL.asBits.asUInt.resized)(23 downto 0).asUInt
  io.dmaCtrl.fireJob := ctrlRegs(DMA_CTRL.asBits.asUInt.resized).msb

  // Frost
  io.frostCtrl.start := ctrlRegs(FROST_CTRL_SIGNALS.asBits.asUInt.resized)(31).addTag(crossClockDomain)
  io.frostCtrl.ready := ctrlRegs(FROST_CTRL_SIGNALS.asBits.asUInt.resized)(30).addTag(crossClockDomain)
  io.frostCtrl.mode := ctrlRegs(FROST_CTRL_SIGNALS.asBits.asUInt.resized)(3 downto 0).addTag(crossClockDomain)
  io.frostCtrl.offsetRd := ctrlRegs(FROST_CTRL_SIGNALS.asBits.asUInt.resized)(11 downto 4).addTag(crossClockDomain)
  io.frostCtrl.offsetWr := ctrlRegs(FROST_CTRL_SIGNALS.asBits.asUInt.resized)(19 downto 12).addTag(crossClockDomain)

  // SHA
  io.shaCtrl.start := ctrlRegs(SHA_CTRL_SIGNALS_0.asBits.asUInt.resized)(31).addTag(crossClockDomain)
  io.shaCtrl.ready := ctrlRegs(SHA_CTRL_SIGNALS_0.asBits.asUInt.resized)(30).addTag(crossClockDomain)
  io.shaCtrl.mode := ctrlRegs(SHA_CTRL_SIGNALS_0.asBits.asUInt.resized)(1 downto 0).addTag(crossClockDomain)
  io.shaCtrl.no_update := ctrlRegs(SHA_CTRL_SIGNALS_0.asBits.asUInt.resized)(3).addTag(crossClockDomain).addTag(crossClockDomain)
  io.shaCtrl.rdOffset := ctrlRegs(SHA_CTRL_SIGNALS_0.asBits.asUInt.resized)(11 downto 4).addTag(crossClockDomain).asUInt
  io.shaCtrl.wrOffset := ctrlRegs(SHA_CTRL_SIGNALS_0.asBits.asUInt.resized)(19 downto 12).addTag(crossClockDomain).asUInt
  io.shaCtrl.work_factor := ctrlRegs(SHA_CTRL_SIGNALS_0.asBits.asUInt.resized)(2).addTag(crossClockDomain)
  io.shaCtrl.work_factor_num := ctrlRegs(SHA_CTRL_SIGNALS_1.asBits.asUInt)


  // Scan chains
  // Core( 12+12 (fine tune) + 3 + 3 (top and bottom fine tune 9bits - MSB, (1:0) to one hot * 2) + 25 bit coarse ) = 24 + 31 = 55
  // core clkselect + RO enable = 2 | Divider = 4 | clock gates = 6
  // total = (31+24) 55 + 2 + 4 + 6 = 67
//  val rstFineTuneTriState = "b0_100_100_100_100_0_100_100_100_100" // All 8X drive 26
//  val rstFineTuneInv = "_000_000" // Minimum delay (skip all stages)
//  val rstCoarseTune = "_0000000000000000000000001" // Thermometer code (1 stage)
//  // core div-by-2, enable RO, no division (0), & enable all 6 outs
//  val rstOther = "_11_0000_111111"
  //val resetVal: UInt = (rstFineTuneTriState + rstFineTuneInv + rstCoarseTune + rstOther).U(69.W)

  val statusRegInits = List(
    B("32'h0000_0000"), // reg0 - SSCA
    B("32'h0000_0000"), // reg1 - DMA
    B("32'h0000_0000"), // reg2 - DLA
    B("32'h0000_0000"), // reg3 - FROST
    B("32'h0000_0000")  // reg3 - SHA
  )
  val statusRegs = Vec(statusRegInits.map(init => RegInit(init)))
  statusRegs(SSCA_STATUS.asBits.asUInt.resized)(0) := BufferCC(False)
  statusRegs(DMA_STATUS.asBits.asUInt.resized) := Cat(B(0, 31 bits), io.dmaCtrl.jobDone)
  statusRegs(FROST_STATUS.asBits.asUInt.resized)(0) := BufferCC(io.frostCtrl.done)
  statusRegs(SHA_STATUS.asBits.asUInt.resized)(0) := BufferCC(io.shaCtrl.done)
//  statusRegs(DL_STATUS.asBits.asUInt.resized) := Cat(B(0, 31 bits), io.fpgaCtrl.doneConfig)

  // CFU response
  io.cfu.cmd.ready := io.cfu.rsp.ready
  io.cfu.rsp.payload.outputs(0) := Mux(funct3 === 0, statusRegs(funct7.asUInt.resized).asBits, B(0, 32 bits))
  io.cfu.rsp.valid := io.cfu.cmd.valid
  io.cfu.rsp.status := B(0).resized

}

object CFUControllerGen{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    config.generateVerilog({
      val toplevel = CfuController(ModRecSocConfig.default.sscaAccelParams.planMemSize / 4)
      toplevel
    })
  }
}