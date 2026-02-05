package vexriscv.ip.modrec

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi.{Axi4Shared, Axi4SharedOnChipRam}

import vexriscv.ip.chipscan._
import vexriscv.ip.tech._
import vexriscv.ip.modrec._

case class Axi4CustomOnChipRamWithScan(dataWidth : Int, byteCount : BigInt, idWidth : Int, arwStage : Boolean = false) extends Component{
  val axiConfig = Axi4SharedOnChipRam.getAxiConfig(dataWidth,byteCount,idWidth)

  val io = new Bundle {
    val axi = slave(Axi4Shared(axiConfig))
    val testMode = in Bool()
    val scanBusVec = Vec(slave(ScanBus(ModRecSocConfig.defaultScanBusConfig)),2)
  }

  // Scan regs
//  require(FlexrviConfig.default.onChipRamSize <= BigInt(256).KiB)
  val scanWriteReg = ScanReg(ScanRegConfig(ModRecSocConfig.defaultScanBusConfig,
    name = "ocmem_wr",
    len = (16+32+2+4),
    withLoad = true
  ))
  val scanWriteRegData = new Bundle {
    val mask = scanWriteReg.io.dataOut(0, 4 bits)
    val write = scanWriteReg.io.dataOut(4, 1 bits)
    val enable = scanWriteReg.io.dataOut(5, 1 bits)
    val data = scanWriteReg.io.dataOut(6, 32 bits)
    val addr = scanWriteReg.io.dataOut(38, 16 bits) // word addr
  }
  val scanReadReg = ScanReg(ScanRegConfig(ModRecSocConfig.defaultScanBusConfig,
    name = "ocmem_rd",
    len = 32,
    withCapture = true
  ))
  scanReadReg.io.scanBus << io.scanBusVec(0)
  scanWriteReg.io.scanBus << io.scanBusVec(1)
  io.scanBusVec(0).dataOut << scanReadReg.io.scanBus.dataOut
  io.scanBusVec(1).dataOut << scanWriteReg.io.scanBus.dataOut

  val wordCount = byteCount / axiConfig.bytePerWord
//  val ram = Mem(axiConfig.dataType, wordCount.toInt)
//  ram.generateAsBlackBox()

  val ram = SPMem(dataWidth, wordCount.toInt, colMux=8, tech="N16", macroType="SHD")

  val wordRange = log2Up(wordCount) + log2Up(axiConfig.bytePerWord)-1 downto log2Up(axiConfig.bytePerWord)

  val arw = if(arwStage) io.axi.arw.s2mPipe().unburstify.m2sPipe() else io.axi.arw.unburstify

  // HAlt stage0 stream when write data is not available
  //  - When awr is a write cmd & there is no write data in w(writeData) channel
  val stage0 = arw.haltWhen(arw.write && !io.axi.writeData.valid)

  ram.io.clk := ClockDomain.current.readClockWire
  ram.io.address := Mux(io.testMode, scanWriteRegData.addr.asUInt.resize(log2Up(wordCount)), stage0.addr(axiConfig.wordRange).resize(log2Up(wordCount)))
  ram.io.data := Mux(io.testMode, scanWriteRegData.data, io.axi.writeData.data)
  ram.io.enable := Mux(io.testMode, scanWriteRegData.enable.asBool, stage0.fire)
  ram.io.write := Mux(io.testMode, scanWriteRegData.write.asBool, stage0.write)
  ram.io.mask := Mux(io.testMode, scanWriteRegData.mask, io.axi.writeData.strb)
  val ramRdData = ram.io.out_data

//  val ramRdData = ram.readWriteSync(
//    address = Mux(io.testMode, scanWriteRegData.addr.asUInt.resize(log2Up(wordCount)), stage0.addr(axiConfig.wordRange).resize(log2Up(wordCount))),
//    data = Mux(io.testMode, scanWriteRegData.data, io.axi.writeData.data),
//    enable = Mux(io.testMode, scanWriteRegData.enable.asBool, stage0.fire),
//    write = Mux(io.testMode, scanWriteRegData.write.asBool, stage0.write),
//    mask = Mux(io.testMode, scanWriteRegData.mask, io.axi.writeData.strb)
//  )
  io.axi.readRsp.data := ramRdData
  scanReadReg.io.dataIn.get := ramRdData

  io.axi.writeData.ready :=  arw.valid && arw.write  && stage0.ready

  val stage1 = stage0.stage()
  // Write response is provided during last write beat.
  // Stall stage1 (awr) (stage1 same ready as stage0=arw_unburst) if wrRsp stream is busy during last beat
  stage1.ready := (io.axi.readRsp.ready && !stage1.write) || ((io.axi.writeRsp.ready || ! stage1.last) && stage1.write)

  io.axi.readRsp.valid  := stage1.valid && !stage1.write
  io.axi.readRsp.id  := stage1.id
  io.axi.readRsp.last := stage1.last
  io.axi.readRsp.setOKAY()
  if(axiConfig.useRUser) io.axi.readRsp.user  := stage1.user

  io.axi.writeRsp.valid := stage1.valid &&  stage1.write && stage1.last
  io.axi.writeRsp.setOKAY()
  io.axi.writeRsp.id := stage1.id
  if(axiConfig.useBUser) io.axi.writeRsp.user := stage1.user

  io.axi.arw.ready.noBackendCombMerge //Verilator perf
}

object OnChipRamScanTest{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    // config.addStandardMemBlackboxing(blackboxOnlyIfRequested)
    config.generateVerilog(Axi4CustomOnChipRamWithScan(
      dataWidth = 32,
      byteCount = ModRecSocConfig.default.onChipRamSize,
      idWidth = 4
    ))
  }
}