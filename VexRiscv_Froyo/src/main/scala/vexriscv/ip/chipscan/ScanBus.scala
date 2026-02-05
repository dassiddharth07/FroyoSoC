package vexriscv.ip.chipscan


import spinal.core._
import spinal.lib.bus.amba3.apb.Apb3Gpio
import spinal.lib._

import scala.math._

case class ScanBusConfig(width: Int = 4, withClock: Boolean = false)

case class ScanRegConfig(busConf: ScanBusConfig,
                         name: String,
                         len: Int,
                         initVal: BigInt = 0,
                         withLoad: Boolean = false,
                         withCapture: Boolean = false)

case class ScanBus(config: ScanBusConfig) extends Bundle with IMasterSlave {
  val dataIn = Flow(Bits(4 bits))
  val dataOut = Flow(Bits(4 bits))
  val load = Bool()
  val read = Bool()
  val reset = Bool()
  val clock = if (config.withClock) Bool() else null

  override def asMaster(): Unit = {
    master(dataIn)
    slave(dataOut)

    out(load)
    out(read)
    out(reset)
    out(clock)
  }

  def <<(that: ScanBus): Unit = that >> this

  /**
   * Connects Scan in (one to many)
   * Does not connect scan out (needs mux)
   * @example{{{ core.io.interrupt = (0 -> uartCtrl.io.interrupt, 1 -> timerCtrl.io.interrupt, default -> false)}}}
   */
  def >>(that: ScanBus): Unit = {
    this.dataIn >> that.dataIn
    //this.dataOut << that.dataOut
    that.load := this.load
    that.read := this.read
    that.reset := this.reset
    if (this.config.withClock && that.config.withClock) {
      that.clock := this.clock
    }
  }

  def driveCtrlWith(that: ScanBus): Unit = {
    //this.dataIn >> that.dataIn
    //this.dataOut << that.dataOut
    this.load := that.load
    this.read := that.read
    this.reset := that.reset
    if (this.config.withClock && that.config.withClock) {
      this.clock := that.clock
    }
  }

//  def connectFrom(that: ScanBus): ScanBus = {
//    that.dataIn << this.dataIn
//
//    that
//  }


}

case class ScanReg(config: ScanRegConfig) extends Component {
  val io = new Bundle {
    val scanBus = slave(ScanBus(config.busConf))
    val dataIn = if (config.withCapture) Some(in(Bits(config.len bits))) else None
    val dataOut = out(Bits(config.len bits))
  }

  val scanClockDomain = ClockDomain(io.scanBus.clock, io.scanBus.reset)

  val busWidth = config.busConf.width
  val regLength = ceil(1.0 * config.len / busWidth).toInt
  val regInitVal = B(config.initVal, regLength*busWidth bits)

  val scanClkArea =  new ClockingArea(scanClockDomain) {

    val scanReg = Vec(Range(0, regLength).map { i =>
      RegInit(regInitVal(i*busWidth, busWidth bits))
    }).setName(s"scanreg_${config.name}").addTag(crossClockDomain)

    //scanReg(regLength-1) := io.scanBus.dataIn.payload

    // Write and read scan reg
    when(io.scanBus.dataIn.fire) {
      Range(0,regLength).foreach {
        case id if id == regLength - 1 => scanReg(id) := io.scanBus.dataIn.payload
        case id => scanReg(id) := scanReg(id + 1)
      }
    }

    //o.dataOut := B(0, io.dataOut.getWidth bits)
    if (config.withCapture) {
      io.dataOut := scanReg.asBits
      when(io.scanBus.read) {
        Range(0, regLength).foreach { i =>
          scanReg(i) := io.dataIn.get((i+1)*4-1 downto i*4)
        }
      }
    } else if (config.withLoad) {
      val shadowReg = RegNextWhen(scanReg.asBits, io.scanBus.load).addTag(crossClockDomain)
      io.dataOut := shadowReg.resize(io.dataOut.getWidth)
    } else {
      io.dataOut := scanReg.asBits
    }
  }

    io.scanBus.dataOut.payload := scanClkArea.scanReg(0)
    io.scanBus.dataOut.valid := io.scanBus.dataIn.fire

}

case class ScanChainCtrl(config: ScanBusConfig, numChains: Int) extends Component {
  val io = new Bundle {
    val scan = slave(ScanBus(config))
    val scanCtrlEn = in Bool()
    val valids = out Bits(numChains bits)
  }

  // Scan reg
  val scanCtrlReg = ScanReg(ScanRegConfig(config,
    name = "Ctrl",
    len = 8
  ))

  scanCtrlReg.io.scanBus.driveCtrlWith(io.scan)
  val scanCtrlFlow = Flow(io.scan.dataIn.payload)
  scanCtrlFlow.valid := io.scanCtrlEn
  scanCtrlFlow.payload := io.scan.dataIn.payload
  scanCtrlReg.io.scanBus.dataIn << scanCtrlFlow
  io.scan.dataOut << scanCtrlReg.io.scanBus.dataOut

  // Decode enables
  Range(0, numChains).foreach( id => io.valids(id) := scanCtrlReg.io.dataOut.asUInt === id)

}

//trait ChipScanRegs {
//
//  val scanBuses: Vec[ScanBus]
//  val scanRegisters: List[ScanReg]
//  val scanBusWidth: Int
//
//}



//object ScanRegTest{
//  def main(args: Array[String]) {
//    val config = SpinalConfig(targetDirectory = "verilog_outputs")
//    config.generateVerilog(new ScanReg(ScanRegConfig(vexriscv.ip.flexrv.FlexrviConfig.defaultScanBusConfig,
//      name = "test_rd",
//      len = 32,
//      withCapture = true
//    )))
//  }
//}