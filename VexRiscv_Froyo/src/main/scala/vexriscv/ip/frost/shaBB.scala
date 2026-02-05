package vexriscv.ip.frost

import scala.util.Random

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.amba4.axilite._
import spinal.lib.fsm._

case class shaBB(cname: String) extends BlackBox {
  setDefinitionName(cname)

  val clk = in Bool()
  val reset_n = in Bool()

  val init = in Bool()
  val next = in Bool()
  val mode = in Bits(2 bits)

  val work_factor = in Bool()
  val work_factor_num = in Bits(32 bits)

  val block = in Bits(1024 bits)

  val ready = out Bool()
  val digest = out Bits(512 bits)
  val digest_valid = out Bool()

  val no_update = in Bool()

}

