//package vexriscv.demo
//
//
//import vexriscv.plugin._
//import vexriscv._
//import vexriscv.ip.{DataCacheConfig, InstructionCacheConfig}
//import spinal.core._
//import spinal.lib._
//import spinal.lib.bus.amba3.apb._
//import spinal.lib.bus.amba4.axi._
//import spinal.lib.com.jtag.Jtag
//import spinal.lib.com.jtag.sim.JtagTcp
//import spinal.lib.com.uart.sim.{UartDecoder, UartEncoder}
//import spinal.lib.com.uart.{Apb3UartCtrl, Uart, UartCtrlGenerics, UartCtrlMemoryMappedConfig}
//import spinal.lib.graphic.RgbConfig
//import spinal.lib.graphic.vga.{Axi4VgaCtrl, Axi4VgaCtrlGenerics, Vga}
//import spinal.lib.io.TriStateArray
//import spinal.lib.memory.sdram.SdramGeneration.SDR
//import spinal.lib.memory.sdram._
//import spinal.lib.memory.sdram.sdr.sim.SdramModel
//import spinal.lib.memory.sdram.sdr.{Axi4SharedSdramCtrl, IS42x320D, SdramInterface, SdramTimings}
//import spinal.lib.misc.HexTools
//import spinal.lib.soc.pinsec.{PinsecTimerCtrl, PinsecTimerCtrlExternal}
//import spinal.lib.system.debugger.{JtagAxi4SharedDebugger, JtagBridge, SystemDebugger, SystemDebuggerConfig}
//import vexriscv.ip.flexrv.{Axi4CustomOnChipRam, CfuAccel}
//import vexriscv.ip.chipscan
//import vexriscv.ip.chipscan.ScanBusConfig
//
//import scala.collection.mutable.ArrayBuffer
//import scala.collection.Seq
//
//
//
//case class FlexrviConfig(axiFrequency : HertzNumber,
//                       onChipRamSize : BigInt,
//                       sdramLayout: SdramLayout,
//                       sdramTimings: SdramTimings,
//                       cpuPlugins : ArrayBuffer[Plugin[VexRiscv]],
//                       uartCtrlConfig : UartCtrlMemoryMappedConfig)
//
//object FlexrviConfig{
//
//  def default = {
//    val config = FlexrviConfig(
//      axiFrequency = 50 MHz,
//      onChipRamSize  = 128 KiB,
//      sdramLayout = IS42x320D.layout,
//      sdramTimings = IS42x320D.timingGrade7,
//      uartCtrlConfig = UartCtrlMemoryMappedConfig(
//        uartCtrlConfig = UartCtrlGenerics(
//          dataWidthMax      = 8,
//          clockDividerWidth = 20,
//          preSamplingSize   = 1,
//          samplingSize      = 5,
//          postSamplingSize  = 2
//        ),
//        txFifoDepth = 16,
//        rxFifoDepth = 16
//      ),
//      cpuPlugins = ArrayBuffer(
//        new PcManagerSimplePlugin(0x80000000l, false),
//        //          new IBusSimplePlugin(
//        //            interfaceKeepData = false,
//        //            catchAccessFault = true
//        //          ),
//        new IBusCachedPlugin(
//          resetVector = 0x80000000l,
//          prediction = STATIC,
//          config = InstructionCacheConfig(
//            cacheSize = 8192,
//            bytePerLine =32,
//            wayCount = 1,
//            addressWidth = 32,
//            cpuDataWidth = 32,
//            memDataWidth = 32,
//            catchIllegalAccess = true,
//            catchAccessFault = true,
//            asyncTagMemory = false,
//            twoCycleRam = true,
//            twoCycleCache = true
//          )
//          //            askMemoryTranslation = true,
//          //            memoryTranslatorPortConfig = MemoryTranslatorPortConfig(
//          //              portTlbSize = 4
//          //            )
//        ),
//        //                    new DBusSimplePlugin(
//        //                      catchAddressMisaligned = true,
//        //                      catchAccessFault = true
//        //                    ),
//        new DBusCachedPlugin(
//          config = new DataCacheConfig(
//            cacheSize         = 4096,
//            bytePerLine       = 32,
//            wayCount          = 1,
//            addressWidth      = 32,
//            cpuDataWidth      = 32,
//            memDataWidth      = 32,
//            catchAccessError  = true,
//            catchIllegal      = true,
//            catchUnaligned    = true
//          ),
//          memoryTranslatorPortConfig = null
//          //            memoryTranslatorPortConfig = MemoryTranslatorPortConfig(
//          //              portTlbSize = 6
//          //            )
//        ),
//        new StaticMemoryTranslatorPlugin(
//          ioRange      = _(31 downto 28) === 0xF
//        ),
//        new DecoderSimplePlugin(
//          catchIllegalInstruction = true
//        ),
//        new RegFilePlugin(
//          regFileReadyKind = plugin.SYNC,
//          zeroBoot = false
//        ),
//        new IntAluPlugin,
//        new SrcPlugin(
//          separatedAddSub = false,
//          executeInsertion = true
//        ),
//        new FullBarrelShifterPlugin,
//        new MulPlugin,
//        new DivPlugin,
//        new HazardSimplePlugin(
//          bypassExecute           = true,
//          bypassMemory            = true,
//          bypassWriteBack         = true,
//          bypassWriteBackBuffer   = true,
//          pessimisticUseSrc       = false,
//          pessimisticWriteRegFile = false,
//          pessimisticAddressMatch = false
//        ),
//        new BranchPlugin(
//          earlyBranch = false,
//          catchAddressMisaligned = true
//        ),
//        new CsrPlugin(
//          config = CsrPluginConfig(
//            catchIllegalAccess = false,
//            mvendorid      = null,
//            marchid        = null,
//            mimpid         = null,
//            mhartid        = null,
//            misaExtensionsInit = 66,
//            misaAccess     = CsrAccess.NONE,
//            mtvecAccess    = CsrAccess.NONE,
//            mtvecInit      = 0x80000020l,
//            mepcAccess     = CsrAccess.READ_WRITE,
//            mscratchGen    = false,
//            mcauseAccess   = CsrAccess.READ_ONLY,
//            mbadaddrAccess = CsrAccess.READ_ONLY,
//            mcycleAccess   = CsrAccess.NONE,
//            minstretAccess = CsrAccess.NONE,
//            ecallGen       = false,
//            wfiGenAsWait         = false,
//            ucycleAccess   = CsrAccess.NONE,
//            uinstretAccess = CsrAccess.NONE
//          )
//        ),
//        new CfuPlugin(
//          stageCount = 1,
//          allowZeroLatency = true,
//          encodings = List(
//            CfuPluginEncoding(
//              instruction = M"-------------------------0001011",
//              functionId = List(14 downto 12),
//              input2Kind = CfuPlugin.Input2Kind.RS
//            )
//          ),
//          busParameter = CfuBusParameter(
//            CFU_VERSION = 0,
//            CFU_INTERFACE_ID_W = 0,
//            CFU_FUNCTION_ID_W = 3,
//            CFU_REORDER_ID_W = 0,
//            CFU_REQ_RESP_ID_W = 0,
//            CFU_INPUTS = 2,
//            CFU_INPUT_DATA_W = 32,
//            CFU_OUTPUTS = 1,
//            CFU_OUTPUT_DATA_W = 32,
//            CFU_FLOW_REQ_READY_ALWAYS = false,
//            CFU_FLOW_RESP_READY_ALWAYS = false,
//            CFU_WITH_STATUS = true,
//            CFU_RAW_INSN_W = 32,
//            CFU_CFU_ID_W = 4,
//            CFU_STATE_INDEX_NUM = 5
//          ),
//          enableInit = true
//        ),
//        new YamlPlugin("cpu0.yaml")
//      )
//    )
//    config
//  }
//}
//
//class Flexrvi(val config: FlexrviConfig) extends Component{
//
//  //Legacy constructor
//  def this(axiFrequency: HertzNumber) {
//    this(FlexrviConfig.default.copy(axiFrequency = axiFrequency))
//  }
//
//  import config._
//  val debug = true
//  val interruptCount = 4
//  def vgaRgbConfig = RgbConfig(5,6,5)
//
//  val io = new Bundle{
//    //Clocks / reset
//    val asyncReset = in Bool()
//    val axiClk     = in Bool()
//
//    //Main components IO
//    val jtag       = slave(Jtag())
//
//    //Peripherals IO
//    val gpioA         = master(TriStateArray(32 bits))
//    val gpioB         = master(TriStateArray(32 bits))
//    val uart          = master(Uart())
//    //val timerExternal = in(PinsecTimerCtrlExternal())
//    val coreInterrupt = in Bool()
//  }
//
//  val resetCtrlClockDomain = ClockDomain(
//    clock = io.axiClk,
//    config = ClockDomainConfig(
//      resetKind = BOOT
//    )
//  )
//
//  val resetCtrl = new ClockingArea(resetCtrlClockDomain) {
//    val systemResetUnbuffered  = False
//    //    val coreResetUnbuffered = False
//
//    //Implement an counter to keep the reset axiResetOrder high 64 cycles
//    // Also this counter will automaticly do a reset when the system boot.
//    val systemResetCounter = Reg(UInt(6 bits)) init(0)
//    when(systemResetCounter =/= U(systemResetCounter.range -> true)){
//      systemResetCounter := systemResetCounter + 1
//      systemResetUnbuffered := True
//    }
//    when(BufferCC(io.asyncReset)){
//      systemResetCounter := 0
//    }
//
//    //Create all reset used later in the design
//    val systemReset  = RegNext(systemResetUnbuffered)
//    val axiReset     = RegNext(systemResetUnbuffered)
//    //val vgaReset     = BufferCC(axiReset)
//  }
//
//  val axiClockDomain = ClockDomain(
//    clock = io.axiClk,
//    reset = resetCtrl.axiReset,
//    frequency = FixedFrequency(axiFrequency) //The frequency information is used by the SDRAM controller
//  )
//
//  val debugClockDomain = ClockDomain(
//    clock = io.axiClk,
//    reset = resetCtrl.systemReset,
//    frequency = FixedFrequency(axiFrequency)
//  )
//
//  val axi = new ClockingArea(axiClockDomain) {
//
//    val ram = Axi4CustomOnChipRam(
//      dataWidth = 32,
//      byteCount = onChipRamSize,
//      idWidth = 4
//    )
//
//    val apbBridge = Axi4SharedToApb3Bridge(
//      addressWidth = 20,
//      dataWidth    = 32,
//      idWidth      = 4
//    )
//
//    val gpioACtrl = Apb3Gpio(
//      gpioWidth = 32,
//      withReadSync = true
//    )
//    val gpioBCtrl = Apb3Gpio(
//      gpioWidth = 32,
//      withReadSync = true
//    )
//    //val timerCtrl = PinsecTimerCtrl()
//    val timerCtrl = new MuraxApb3Timer()
//
//    val uartCtrl = Apb3UartCtrl(uartCtrlConfig)
//    uartCtrl.io.apb.addAttribute(Verilator.public)
//
//    // CFU accelerator
//    val cfuAccel = CfuAccel()
//
//    val core = new Area{
//      val config = VexRiscvConfig(
//        plugins = cpuPlugins += new DebugPlugin(debugClockDomain)
//      )
//
//      val cpu = new VexRiscv(config)
//      var iBus : Axi4ReadOnly = null
//      var dBus : Axi4Shared = null
//      var cfuBus : CfuBus = null
//
//      for(plugin <- config.plugins) plugin match{
//        case plugin : IBusSimplePlugin => iBus = plugin.iBus.toAxi4ReadOnly()
//        case plugin : IBusCachedPlugin => iBus = plugin.iBus.toAxi4ReadOnly()
//        case plugin : DBusSimplePlugin => dBus = plugin.dBus.toAxi4Shared()
//        case plugin : DBusCachedPlugin => dBus = plugin.dBus.toAxi4Shared(false)
//        case plugin : CsrPlugin        => {
//          plugin.externalInterrupt := BufferCC(io.coreInterrupt)
//          plugin.timerInterrupt := timerCtrl.io.interrupt
//        }
//        case plugin : DebugPlugin      => debugClockDomain{
//          resetCtrl.axiReset setWhen(RegNext(plugin.io.resetOut))
//          io.jtag <> plugin.io.bus.fromJtag()
//        }
//        case plugin:  CfuPlugin => cfuBus = plugin.bus
//        case _ =>
//      }
//    }
//
//    // Connect CFU
//    cfuAccel.io.cfu <> core.cfuBus
//    cfuAccel.io.clk := io.axiClk
//
//
//    val axiCrossbar = Axi4CrossbarFactory()
//
//    axiCrossbar.addSlaves(
//      ram.io.axi       -> (0x80000000L,   onChipRamSize),
//      //sdramCtrl.io.axi -> (0x40000000L,   sdramLayout.capacity),
//      apbBridge.io.axi -> (0xF0000000L,   1 MB)
//    )
//
//    axiCrossbar.addConnections(
//      core.iBus       -> List(ram.io.axi),
//      core.dBus       -> List(ram.io.axi, apbBridge.io.axi)
//      //core.iBus       -> List(ram.io.axi, sdramCtrl.io.axi),
//      //core.dBus       -> List(ram.io.axi, sdramCtrl.io.axi, apbBridge.io.axi)
//      //,
//      //vgaCtrl.io.axi  -> List(            sdramCtrl.io.axi)
//    )
//
//
//    axiCrossbar.addPipelining(apbBridge.io.axi)((crossbar,bridge) => {
//      crossbar.sharedCmd.halfPipe() >> bridge.sharedCmd
//      crossbar.writeData.halfPipe() >> bridge.writeData
//      crossbar.writeRsp             << bridge.writeRsp
//      crossbar.readRsp              << bridge.readRsp
//    })
//
//    //axiCrossbar.addPipelining(sdramCtrl.io.axi)((crossbar,ctrl) => {
//    //  crossbar.sharedCmd.halfPipe()  >>  ctrl.sharedCmd
//    //  crossbar.writeData            >/-> ctrl.writeData
//    //  crossbar.writeRsp              <<  ctrl.writeRsp
//    //  crossbar.readRsp               <<  ctrl.readRsp
//    //})
//
//    axiCrossbar.addPipelining(ram.io.axi)((crossbar,ctrl) => {
//      //crossbar.sharedCmd.halfPipe()  >>  ctrl.sharedCmd
//      crossbar.sharedCmd            >/->  ctrl.sharedCmd
//      crossbar.writeData            >/-> ctrl.writeData
//      crossbar.writeRsp              <<  ctrl.writeRsp
//      crossbar.readRsp               <<  ctrl.readRsp
//    })
//
//    //axiCrossbar.addPipelining(vgaCtrl.io.axi)((ctrl,crossbar) => {
//    //  ctrl.readCmd.halfPipe()    >>  crossbar.readCmd
//    //  ctrl.readRsp               <<  crossbar.readRsp
//    //})
//
//    axiCrossbar.addPipelining(core.dBus)((cpu,crossbar) => {
//      cpu.sharedCmd             >>  crossbar.sharedCmd
//      cpu.writeData             >>  crossbar.writeData
//      cpu.writeRsp              <<  crossbar.writeRsp
//      cpu.readRsp               <-< crossbar.readRsp //Data cache directly use read responses without buffering, so pipeline it for FMax
//    })
//
//    axiCrossbar.build()
//
//
//    val apbDecoder = Apb3Decoder(
//      master = apbBridge.io.apb,
//      slaves = List(
//        gpioACtrl.io.apb -> (0x00000, 4 kB),
//        gpioBCtrl.io.apb -> (0x01000, 4 kB),
//        uartCtrl.io.apb  -> (0x10000, 4 kB),
//        timerCtrl.io.apb -> (0x20000, 4 kB)
//        //,
//        //vgaCtrl.io.apb   -> (0x30000, 4 kB)
//      )
//    )
//  }
//
//  io.gpioA          <> axi.gpioACtrl.io.gpio
//  io.gpioB          <> axi.gpioBCtrl.io.gpio
//  io.uart           <> axi.uartCtrl.io.uart
//
//}
//
//object FlexrviCFU{
//  def main(args: Array[String]) {
//    val config = SpinalConfig(targetDirectory = "verilog_outputs")
//   // config.addStandardMemBlackboxing(blackboxOnlyIfRequested)
//    config.generateVerilog({
//      val toplevel = new Flexrvi(FlexrviConfig.default)
////      toplevel.rework {
////        for (plugin <- toplevel.axi.core.cpu.plugins) plugin match {
////          case plugin: CfuPlugin => {
////            plugin.bus.toIo().setName("cfu")
////            //println("Found CFU plugin")
////          }
////          case _ =>
////        }
////      }
//      toplevel
//    })
//  }
//}
//
//////DE1-SoC with memory init
////object FlexrviWithMemoryInit{
////  def main(args: Array[String]) {
////    val config = SpinalConfig()
////    config.generateVerilog({
////      val toplevel = new Flexrvi(FlexrviConfig.default)
////      //toplevel.axi.vgaCtrl.vga.ctrl.io.error.addAttribute(Verilator.public)
////      //toplevel.axi.vgaCtrl.vga.ctrl.io.frameStart.addAttribute(Verilator.public)
////      HexTools.initRam(toplevel.axi.ram.ram, "src/main/ressource/hex/muraxDemo.hex", 0x80000000l)
////      toplevel
////    })
////  }
////}
//
//
//
