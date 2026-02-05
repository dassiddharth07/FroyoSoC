package vexriscv.ip.modrec

import spinal.core._
import spinal.lib._

case class Clkgen() extends BlackBox {

  setDefinitionName("clkgen")

  val io = new Bundle {
    // Functional
    val slowClock = in Bool()
    val fastClockSel = in Bool()
    val rst = in Bool()


    val scanIn = in Bool()
    val scanLoad = in Bool()
    val scanClock = in Bool()
    val scanOut  = out Bool()
    val scanCapture = in Bool()
    val scanReadOut = out Bool()
    val refClock = in Bool()
    val measure = in Bool()

    val clkOut = out Bool()
    val clkOuts = out Bits(6 bits)
  }

  noIoPrefix()
  //addRTLPath("")
  //Map the current clock domain to the io.clk pin
  //mapClockDomain(clock=io.scanClock)
}