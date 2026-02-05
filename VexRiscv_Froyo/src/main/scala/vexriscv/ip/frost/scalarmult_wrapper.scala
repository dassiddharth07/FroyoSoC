package vexriscv.ip.frost

import scala.util.Random

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.amba4.axilite._
import spinal.lib.fsm._

case class scalarmult_wrapper() extends Component {

  val io = new Bundle{
    val start = in Bool()
    val done = out Bool()
    val ready = in Bool()

    val mode = in Bits(4 bits)
    val offsetRd = in Bits(8 bits)
    val offsetWr = in Bits(8 bits)


    val rdAddr = out Bits (9 bits)
    val wrAddr = out Bits (9 bits)

//    val size_A1 = in Bits(4 bits)
//    val size_A2 = in Bits(4 bits)


    val rdData = in Bits (512 bits)

    val readEnable = out Bool()
    val writeEnable = out Bool()


    val wrData = out Bits (512 bits)

  }

  val clk = in Bool()
  val rst = in Bool()

  val scalarMult = new scalar_mult()

  scalarMult.io.start := io.start
  scalarMult.io.memP_X := io.rdData(511 downto 256)
  scalarMult.io.memP_Y := io.rdData(255 downto 0)
  scalarMult.clk := clk
  scalarMult.rst := rst
  scalarMult.io.mode := io.mode
  scalarMult.io.size_A1 := B(15, 4 bits)
  scalarMult.io.size_A2 := B(15, 4 bits)
  scalarMult.io.offsetWr := io.offsetWr
  scalarMult.io.ready := io.ready

  io.rdAddr := (scalarMult.io.rdAddrPXY.asUInt + io.offsetRd.asUInt).resize(9 bits).asBits
  io.wrAddr := (scalarMult.io.wrCheck)? scalarMult.io.wrAddrPXY.resize(9 bits) | (scalarMult.io.wrAddrPXY.asUInt + io.offsetRd.asUInt).resize(9 bits).asBits
  io.readEnable := scalarMult.io.readEnable
  io.writeEnable := scalarMult.io.writeEnable
  io.wrData := Cat(scalarMult.io.wrMem_X, scalarMult.io.wrMem_Y)

  io.done := scalarMult.io.done

}

object ScalarMultGen1{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    config.generateVerilog({
      val toplevel = new scalarmult_wrapper()
      toplevel
    })
  }
}