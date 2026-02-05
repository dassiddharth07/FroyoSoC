package vexriscv.ip.modrec

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi.{Axi4Shared, Axi4SharedOnChipRam}

case class Axi4CustomSharedOnChipRam(dataWidth : Int, byteCount : BigInt, idWidth : Int, arwStage : Boolean = false) extends Component{
  val axiConfig = Axi4SharedOnChipRam.getAxiConfig(dataWidth,byteCount,idWidth)

  println(s"Shared RAM bytes $byteCount")
  println(s"Shared RAM address width ${log2Up(byteCount/4)}")
  val io = new Bundle {
    val axi = slave(Axi4Shared(axiConfig))
    val sharedMemPort = master(MemBus(32, log2Up(byteCount/4), dataWidth/8))
  }

  val wordCount = byteCount / axiConfig.bytePerWord
  //val ram = Mem(axiConfig.dataType,wordCount.toInt)
  //ram.generateAsBlackBox()
  val wordRange = log2Up(wordCount) + log2Up(axiConfig.bytePerWord)-1 downto log2Up(axiConfig.bytePerWord)

  val arw = if(arwStage) io.axi.arw.s2mPipe().unburstify.m2sPipe() else io.axi.arw.unburstify

  // HAlt stage0 stream when write data is not available
  //  - When awr is a write cmd & there is no write data in w(writeData) channel
  // Add mem bus ready signal to stall incomming command
  val stage0 = arw.haltWhen(arw.write && !io.axi.writeData.valid && io.sharedMemPort.cmd.ready)

  // Mem connections
//  io.axi.readRsp.valid := io.sharedMemPort.rsp.valid
  io.axi.readRsp.data := io.sharedMemPort.rsp.data
  io.sharedMemPort.rsp.ready := True

  io.sharedMemPort.cmd.address :=  (stage0.addr>>2).resized
  io.sharedMemPort.cmd.data :=  io.axi.writeData.data
  io.sharedMemPort.cmd.valid := stage0.fire
  io.sharedMemPort.cmd.write := stage0.write
  io.sharedMemPort.cmd.mask := io.axi.writeData.strb



  // First wait for write command (arw) and make sure you are ready to process data accepting write data
  io.axi.writeData.ready :=  arw.valid && arw.write  && stage0.ready
  val stage1 = stage0.stage()
  // Write response is provided during last write beat.
  // Stall stage1 (awr) (stage1 same ready as stage0=arw_unburst) if wrRsp stream is busy during last beat
  stage1.ready := (io.axi.readRsp.ready && !stage1.write) || ((io.axi.writeRsp.ready || ! stage1.last) && stage1.write)

  io.axi.readRsp.valid  := stage1.valid && !stage1.write
//  assert(
//    assertion = !(io.axi.readRsp.valid.fall && io.sharedMemPort.rsp.valid),
//    message = "Valid dropped when ready was low",
//    severity = ERROR
//  )
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

object ModRecSharedOCMGen{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    // config.addStandardMemBlackboxing(blackboxOnlyIfRequested)
    config.generateVerilog(Axi4CustomSharedOnChipRam(
      dataWidth = 32,
      byteCount = ModRecSocConfig.default.onChipRamSize,
      idWidth = 4,
      arwStage = true
    ))
  }
}