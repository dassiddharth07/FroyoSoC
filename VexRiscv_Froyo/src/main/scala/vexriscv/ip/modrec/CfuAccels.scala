package vexriscv.ip.modrec

import spinal.core
import spinal.core.Component.push
import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi.Axi4
import spinal.lib.io.TriStateArray
import vexriscv.plugin.{CfuBus, CfuBusParameter, CfuCmd}
import spinal.lib.bus.simple._
import vexriscv.ip.modrec.ModRecSocConfig
import vexriscv.ip.frost._
import vexriscv.ip.tech._

case class CfuAccels(modRecSocConfig: ModRecSocConfig) extends Component {

//  val (dataWidth, iobramWordCount, confMemWordCount) = (32, BigInt(4).KiB/4, confMemSize/(6*4))

  val dataWidth = modRecSocConfig.sscaAccelParams.dataWidth
  val dataMemPerBankWordCount = modRecSocConfig.sscaAccelParams.dataMemBankSize / 4
  val planMemWordCount = modRecSocConfig.sscaAccelParams.planMemSize / 4
//  val inputMemWordCount = modRecSocConfig.sscaAccelParams(3) * 2 / 4
  val inputMemPerBankWordCount = modRecSocConfig.sscaAccelParams.inputMemBankSize / 4
  val inputMemWordCount = (64+inputMemPerBankWordCount) * 2 // Double buffered input mem



  val dataMemBanks = modRecSocConfig.sscaAccelParams.dataMemBanks
  val dataMemGroups = modRecSocConfig.sscaAccelParams.dataMemGroups
  val maskWidth = dataWidth / 8

  val constMemPerBankWordCount = modRecSocConfig.sscaAccelParams.constMemBankSize / 4
  val constMemWordCount = constMemPerBankWordCount * dataMemBanks

  val io = new Bundle {

    val axiClk = in Bool()
    val accelClk = in Bool()
    val accelReset = in Bool()
    val axiReset = in Bool()

    // CFU port
    val cfu = spinal.lib.slave(CfuBus(ModRecSocConfig.default.cfuBusParam))

    // Mem ports
//    val inputMemBus = master(MemBus(dataWidth, log2Up(inputMemWordCount)))
//    val planMemBus = master(MemBus(dataWidth, log2Up(planMemWordCount)))
//    val dataMemBuses = Vec(Vec(master(Mem2PortBus(dataWidth, log2Up(dataMemPerBankWordCount))), dataMemBanks), dataMemGroups)
//    val constMemBus = Vec(master(MemBus(dataWidth, log2Up(constMemPerBankWordCount))), dataMemBanks)
//    val fpgaReadOnlyMem = master(MemBus(dataWidth, log2Up(iobramWordCount), maskWidth))
//    val fpgaWriteOnlyMem = master(MemBus(dataWidth, log2Up(iobramWordCount), maskWidth))
//    val confMemBuses = Vec(master(MemBus(dataWidth, log2Up(confMemWordCount), maskWidth)), 6)
//    val bramMemBuses = Vec(master(Mem2PortBus(dataWidth, log2Up(fpgaBramSize/4), maskWidth)), 20)

//    val planMemAccessEn = out Bool()
    val dataMemAccessEn = out Bits(2 bits)

//    val gpioAccel = master(TriStateArray(8 bits))
    val accelClockGenTest = out Bool()

    val dmaIrq = in Bool()
    val dmaJobDone = in Bool()

//    val accelCtrl = new Bundle {
//      //      val ctrlIns = in Bits (32 bits)
//      //      val statusOuts = out Bits (32 bits)
//      val arst = in Bool()
//      //      val loopBreak = in Bool()
//      //      val gndBlkOuts = in Bool()
//      //      val latchEn = in Bool()
//      val start = in Bool()
//      val done = out Bool()
////      val planStartAddr = in UInt (log2Up(planMemWordCount) bits)
//    }

    // DLA AXI port
//    val dlaMAXI = master(Axi4(Axi4CoreConfig.getAxi4Config))

    // Frost Mem Ports
    val frostMem = new Bundle {
      val rdAddr = out Bits (9 bits)
      val wrAddr = out Bits (9 bits)


      val rdData = in Bits (512 bits)

      val readEnable = out Bool()
      val writeEnable = out Bool()


      val wrData = out Bits (512 bits)
    }

    val shaMem = new Bundle {
      val rdAddr = out Bits (9 bits)
      val wrAddr = out Bits (9 bits)

      val rdData = in Bits (512 bits)

      val readEnable = out Bool()
      val writeEnable = out Bool()


      val wrData = out Bits (512 bits)
    }
  }

  val axiClockDomain = ClockDomain(io.axiClk, io.axiReset)
//  val clockGen = AccelClockGenerator()

  // CFU Manager
  val cfuController = axiClockDomain(CfuController(planMemWordCount))
//  val accelClkMux = AsicClkMux(cfuController.io.clkGenSelect, clockGen.io.clkOut, io.accelClk, ModRecSocConfig.tech)

  val accelClkMux = io.accelClk

  val accelClockDomain = ClockDomain(accelClkMux, io.accelReset)
  // CFU Manager (ID = 0)

  val managerCmdStream = cloneOf(io.cfu.cmd)
  val sscaAccelCmdStream = cloneOf(io.cfu.cmd)
  val dlaAccelCmdStream = cloneOf(io.cfu.cmd)
//  Vec(managerCmdStream, sscaAccelCmdStream, dlaAccelCmdStream) <> StreamDemux(
//    io.cfu.cmd,
//    io.cfu.cmd.cfu_index(1 downto 0),
//    3
//  )
  managerCmdStream << io.cfu.cmd
  cfuController.io.cfu.cmd << managerCmdStream

  // DLA
//  val dlaAccelRspStream = cloneOf(io.cfu.rsp)
//  val dla = new Dla
//  dla.io.accelClk := io.axiClk
//  dla.io.accelReset := io.accelReset
//  dla.io.cfu.cmd <> dlaAccelCmdStream
//  dla.io.cfu.rsp <> dlaAccelRspStream
//  io.dlaMAXI <> dla.io.m_axi
//  dla.io.cfu.cmd.valid := cfuAccels.dlaAccelCmdStream.valid.pull()
//  cfuAccels.dlaAccelCmdStream.ready.pull() := dla.io.cfu.cmd.ready

  // DMA
  cfuController.io.dmaCtrl.jobDone := io.dmaJobDone
  cfuController.io.dmaCtrl.irq := io.dmaIrq

  // Accel ClockGen connections
//  clockGen.io.slowClock := io.accelClk
//  clockGen.io.fastClockSel := cfuController.io.clkGenfastClockSel
//  clockGen.io.rst := cfuController.io.clkGenReset || io.axiReset
//  clockGen.io.refClock := False
//  clockGen.io.config := cfuController.io.clkGenConfig
//  io.accelClockGenTest := clockGen.io.clkOutTest

  io.accelClockGenTest := io.accelClk

  // SSCA CFU (DI = 1) FPGA clock domain
//  val sscaAccel = SscaAccel(dataWidth, dataMemPerBankWordCount, planMemWordCount, inputMemWordCount, constMemPerBankWordCount, dataMemBanks)
//  sscaAccel.io.accelClk := accelClkMux
//  sscaAccel.io.accelReset := io.accelReset
//  sscaAccel.io.axiClk := io.axiClk
//  sscaAccel.io.accelCtrl.start := io.accelCtrl.start | cfuManager.io.sscaAccelCtrl.start
//  sscaAccel.io.accelCtrl.arst := io.accelCtrl.arst | cfuManager.io.sscaAccelCtrl.arst
//  sscaAccel.io.accelCtrl.planStartAddr := cfuManager.io.sscaAccelCtrl.planStartAddr
//  sscaAccel.io.accelCtrl.planStartAddr1 := cfuManager.io.sscaAccelCtrl.planStartAddr1
//  sscaAccel.io.cfu.cmd << accelCmdStream
//  io.accelCtrl.done := sscaAccel.io.accelCtrl.done
//  cfuManager.io.sscaAccelCtrl.done := axiClockDomain(BufferCC(sscaAccel.io.accelCtrl.done))


  // Frost CFU
  val frostAccel = scalarmult_wrapper()
  frostAccel.clk := accelClkMux
  frostAccel.rst := io.accelReset
  frostAccel.io.start := cfuController.io.frostCtrl.start
  frostAccel.io.mode  := cfuController.io.frostCtrl.mode
  frostAccel.io.offsetRd  := cfuController.io.frostCtrl.offsetRd
  frostAccel.io.offsetWr  := cfuController.io.frostCtrl.offsetWr
  frostAccel.io.ready := cfuController.io.frostCtrl.ready
  cfuController.io.frostCtrl.done := axiClockDomain(BufferCC(frostAccel.io.done))

  // SHA CFU
  val shaAccel = sha()
  shaAccel.clk := accelClkMux
  shaAccel.rst := io.accelReset
  shaAccel.io.start := cfuController.io.shaCtrl.start
  shaAccel.io.ready := cfuController.io.shaCtrl.ready
  shaAccel.io.mode := cfuController.io.shaCtrl.mode
  shaAccel.io.no_update := cfuController.io.shaCtrl.no_update

  shaAccel.io.rdOffset := cfuController.io.shaCtrl.rdOffset
  shaAccel.io.wrOffset := cfuController.io.shaCtrl.wrOffset

  shaAccel.io.work_factor := cfuController.io.shaCtrl.work_factor
  shaAccel.io.work_factor_num := cfuController.io.shaCtrl.work_factor_num
  cfuController.io.shaCtrl.done := shaAccel.io.done

//  io.gpioAccel <> sscaAccel.io.gpio

  // SSCA CFU Command
//  val sscaAccelSyncCmdStream = cloneOf(io.cfu.cmd)
//  val sscaAccelAsyncCmdStream = cloneOf(io.cfu.cmd)
//  Vec(sscaAccelAsyncCmdStream, sscaAccelSyncCmdStream) <> StreamDemux(
//    sscaAccelCmdStream,
//    cfuManager.io.sscaAccelCtrl.cfuSyncMode.asUInt,
//    2
//  )
//  sscaAccel.io.cfu.cmd << StreamMux(cfuManager.io.sscaAccelCtrl.cfuSyncMode.asUInt,
//    Vec(sscaAccelAsyncCmdStream.queue(2,axiClockDomain,accelClockDomain), sscaAccelSyncCmdStream)
//  )
//
//  // Split rsp to sync and async
//  val sscaAccelSyncRspStream = cloneOf(io.cfu.rsp)
//  val sscaAccelAsyncRspStream = cloneOf(io.cfu.rsp)
//  Vec(sscaAccelAsyncRspStream, sscaAccelSyncRspStream) <> StreamDemux(
//    sscaAccel.io.cfu.rsp,
//    cfuManager.io.sscaAccelCtrl.cfuSyncMode.asUInt,
//    2
//  )
//  val cfuSscaAccelRsp = StreamMux(cfuManager.io.sscaAccelCtrl.cfuSyncMode.asUInt,
//    Vec(sscaAccelAsyncRspStream.queue(2, accelClockDomain, axiClockDomain), sscaAccelSyncRspStream)
//  )

//  fpga.io.fpgaCtrl.ctrlIns := fpgaClockDomain(BufferCC(cfuManager.io.fpgaCtrl.ctrlIO)) // CFU register
//  cfuManager.io.fpgaCtrl.statusIO := axiClockDomain(BufferCC(fpga.io.fpgaCtrl.statusOuts))
//

  // SSCA Accel Memories
//  sscaAccel.io.planMemBus >> io.planMemBus
//  sscaAccel.io.inputMemBus >> io.inputMemBus
//  sscaAccel.io.constMemBus.zipWithIndex foreach { case (constMemBus, i) =>
//    constMemBus >> io.constMemBus(i)
//  }
//  sscaAccel.io.dataMemBuses.zipWithIndex.foreach { case (dataMemBus, i) =>
//    dataMemBus.zipWithIndex.foreach { case (dataBankBus, j) =>
//      dataBankBus >> io.dataMemBuses(i)(j)
//    }
//  }
//  io.planMemAccessEn := cfuManager.io.sscaAccelCtrl.planMemAccessEn
  io.dataMemAccessEn := cfuController.io.sscaAccelCtrl.dataMemAccessEn

  // CFUresponse
  io.cfu.rsp << cfuController.io.cfu.rsp

//  io.cfu.rsp << cfuManager.io.cfu.rsp

  // Frost Memories
  io.frostMem.rdAddr      := frostAccel.io.rdAddr
  io.frostMem.readEnable  := frostAccel.io.readEnable
  io.frostMem.wrAddr      := frostAccel.io.wrAddr
  io.frostMem.writeEnable := frostAccel.io.writeEnable
  io.frostMem.wrData      := frostAccel.io.wrData
  frostAccel.io.rdData    := io.frostMem.rdData

  // SHA Memories
  io.shaMem.rdAddr        := shaAccel.io.rdAddr
  io.shaMem.readEnable    := shaAccel.io.readEnable
  io.shaMem.wrAddr        := shaAccel.io.wrAddr
  io.shaMem.writeEnable   := shaAccel.io.writeEnable
  io.shaMem.wrData        := shaAccel.io.wrData
  shaAccel.io.rdData      := io.shaMem.rdData



  //  val fpga = Fpga(dataWidth, iobramWordCount.toInt, confMemWordCount, fpgaBramSize)
  //
  //  fpga.io.fpgaClk := fpgaClkMux
  //  fpga.io.fpgaReset := io.accelReset
  //  fpga.io.axiClk := io.axiClk
  //  fpga.io.axiReset := io.axiReset
  //  fpga.io.fpgaConstClk := io.fpgaCtrlSignals.constClk
  //  fpga.io.fpgaCtrl.arst := io.fpgaCtrlSignals.arst | cfuManager.io.fpgaCtrl.arst
  //  fpga.io.fpgaCtrl.loopBreak := io.fpgaCtrlSignals.loopBreak | cfuManager.io.fpgaCtrl.loopBreak
  //  fpga.io.fpgaCtrl.gndBlkOuts := io.fpgaCtrlSignals.gndBlkOuts | cfuManager.io.fpgaCtrl.gndBlkOuts
  //  fpga.io.fpgaCtrl.latchEn := cfuManager.io.fpgaCtrl.latchEn
  //  fpga.io.fpgaCtrl.startConfig := cfuManager.io.fpgaCtrl.startConfig
  //  fpga.io.fpgaCtrl.confStartAddr := cfuManager.io.fpgaCtrl.confStartAddr
  //  cfuManager.io.fpgaCtrl.doneConfig := fpga.io.fpgaCtrl.doneConfig

}

object CfuAccelsGen{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    config.generateVerilog({
      val toplevel = new CfuAccels(ModRecSocConfig.default)
      toplevel
    })
  }
}


