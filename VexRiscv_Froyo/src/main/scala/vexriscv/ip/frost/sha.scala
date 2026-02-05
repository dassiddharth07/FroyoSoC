package vexriscv.ip.frost

import scala.util.Random

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.amba4.axilite._
import spinal.lib.fsm._

case class sha() extends Component {

  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()

    val ready = in Bool()

    val mode = in Bits (2 bits)
//    val size = in UInt (10 bits)

    val rdOffset = in UInt(8 bits)
    val wrOffset = in UInt(8 bits)

    val work_factor = in Bool()
    val work_factor_num = in Bits(32 bits)

    val rdAddr = out Bits (9 bits)
    val wrAddr = out Bits (9 bits)

    val rdData = in Bits (512 bits)

    val readEnable = out Bool()
    val writeEnable = out Bool()


    val wrData = out Bits (512 bits)

    val no_update = in Bool()

  }

  val clk = in Bool()
  val rst = in Bool()

  val shaInst = new shaBB("sha512_core")

  shaInst.clk := clk
  shaInst.reset_n := ~rst
  shaInst.mode := io.mode
  shaInst.next := False
  shaInst.no_update := io.no_update

  shaInst.work_factor := io.work_factor
  shaInst.work_factor_num := io.work_factor_num


//  val bitsRel = UInt(10 bits)
//  bitsRel := (U(1023, 10 bits) - io.size).resize(10 bits)

  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {

    val startReg = Reg(Bool())
    val allDone = Reg(Bool())

    val cntRd1Retrieve = Reg(UInt(2 bits))
    val cntWr1Retrieve = Reg(UInt(2 bits))

    val flag = Reg(Bool())

    val addrRd = Bits(8 bits)
    val block = Reg(Bits(1024 bits))

    shaInst.block := block

    val opsFSM = new StateMachine {
      val initST = new State with EntryPoint
      val retrieveData = new State
      val sha512ST = new State
      val writeST = new State
      val doneST = new State

      startReg := False

      flag := flag

      block := block

      shaInst.init := False

      io.rdAddr := B(0, 9 bits)
      io.readEnable := False

      io.wrAddr := B(0, 9 bits)
      io.writeEnable := False
      io.wrData := B(0, 512 bits)

      allDone := allDone
      io.done := RegNext(allDone)


      initST
        .onEntry {
          flag := False
          block := B(0, 1024 bits)
        }
        .whenIsActive {
          cntRd1Retrieve := 0
          cntWr1Retrieve := 0
          startReg := io.start
          allDone := False
          when(startReg === True) {
            goto(retrieveData)
          }
        }

      retrieveData
        .onEntry {
          cntRd1Retrieve := cntRd1Retrieve + 1
          io.rdAddr := io.rdOffset.asBits.resized
          io.readEnable := True
        }
        .whenIsActive{
          cntRd1Retrieve := cntRd1Retrieve + 1
          when(cntRd1Retrieve === 1) {
            block := io.rdData.resize(1024 bits)
            io.rdAddr := (io.rdOffset + 1).asBits.resized
            io.readEnable := True
          }
          when(cntRd1Retrieve === 2) {
            cntRd1Retrieve := cntRd1Retrieve + 1
            block := Cat(io.rdData, block(511 downto 0))
          }
          when(cntRd1Retrieve === 3) {
            goto(sha512ST)
          }
        }
      sha512ST
        .onEntry{
          shaInst.init := True
        }
        .whenIsActive{
          when(shaInst.digest_valid === True){
            allDone := True
            goto(writeST)
          }
        }
      writeST
        .onEntry{
          cntWr1Retrieve := cntWr1Retrieve + 1
          io.wrAddr := io.wrOffset.asBits.resized
          io.writeEnable := True
          io.wrData := shaInst.digest(511 downto 0)
        }
        .whenIsActive {
          cntWr1Retrieve := cntWr1Retrieve + 1
          goto(doneST)
          allDone := True
        }
      doneST
        .whenIsActive{
          when(io.ready){
            goto(initST)
          }
        }


    }

  }

}

object ShaGen{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    config.generateVerilog({
      val toplevel = new sha()
      toplevel
    })
  }
}

