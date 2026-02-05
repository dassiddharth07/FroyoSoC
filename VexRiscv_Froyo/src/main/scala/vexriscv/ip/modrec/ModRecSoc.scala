package vexriscv.ip.modrec

import dma_unit.DmaUnit
import vexriscv.plugin._
import vexriscv._
import vexriscv.ip.{DataCacheConfig, InstructionCacheConfig}
import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb._
import spinal.lib.bus.amba4.axi._
import spinal.lib.com.jtag.Jtag
import spinal.lib.com.jtag.sim.JtagTcp
import spinal.lib.com.uart.sim.{UartDecoder, UartEncoder}
import spinal.lib.com.uart.{Apb3UartCtrl, Uart, UartCtrlGenerics, UartCtrlMemoryMappedConfig}
import spinal.lib.graphic.RgbConfig
import spinal.lib.graphic.vga.{Axi4VgaCtrl, Axi4VgaCtrlGenerics, Vga}
import spinal.lib.io.TriStateArray
import spinal.lib.memory.sdram.SdramGeneration.SDR
import spinal.lib.memory.sdram._
import spinal.lib.memory.sdram.sdr.sim.SdramModel
import spinal.lib.memory.sdram.sdr.{Axi4SharedSdramCtrl, IS42x320D, SdramInterface, SdramTimings}
import spinal.lib.misc.HexTools
import spinal.lib.soc.pinsec.{PinsecTimerCtrl, PinsecTimerCtrlExternal}
import spinal.lib.system.debugger.{JtagAxi4SharedDebugger, JtagBridge, SystemDebugger, SystemDebuggerConfig}
import vexriscv.demo.MuraxApb3Timer
import vexriscv.ip.chipscan.ScanBusConfig
import ip.fpu._

import scala.collection.mutable.ArrayBuffer
import scala.collection.Seq
import vexriscv.ip.chipscan._

case class ModRecSocConfig(axiFrequency : HertzNumber,
                       onChipRamSize : BigInt,
//                       sharedOnChipRamSize: BigInt,
                       sscaAccelParams: SscaAccelParams,
                       cfuBusParam: CfuBusParameter,
                       sdramLayout: SdramLayout,
                       sdramTimings: SdramTimings,
                       cpuPlugins : ArrayBuffer[Plugin[VexRiscv]],
                       uartCtrlConfig : UartCtrlMemoryMappedConfig)


case class SscaAccelParams(dataWidth: Int,
                           dataMemBankSize: Int,
                           planMemSize: Int,
                           inputMemBankSize: Int,
                           dataMemBanks: Int,
                           dataMemGroups: Int,
                           constMemBankSize: Int) {

  // All sizes in bytes
  def sharedOnChipRamSize: BigInt = dataMemBankSize * dataMemBanks * dataMemGroups +
    (inputMemBankSize + 64*4) * 2 + planMemSize + constMemBankSize * dataMemBanks
}
object ModRecSocConfig{

  def tech = "N16" // For tech specific cells
  def defaultScanBusConfig = { ScanBusConfig(4, true) }
  def default = {
    val config = ModRecSocConfig(
      axiFrequency = 50 MHz,
      onChipRamSize  = 192 KiB,
//      sharedOnChipRamSize = BigInt(scala.math.pow(2,log2Up(48)).toInt).KiB*2, // ConfMem + 1 bit for BRAM
      sscaAccelParams = SscaAccelParams(
        dataWidth = 32,
        dataMemBankSize =  1 << 16,
        planMemSize = 4096,
        inputMemBankSize = 1 << 14,
        dataMemBanks = 1,
        dataMemGroups = 1,
        constMemBankSize = 1024*4
      ),
      cfuBusParam = CfuBusParameter(
        CFU_VERSION = 0,
        CFU_INTERFACE_ID_W = 0,
        CFU_FUNCTION_ID_W = 3,
        CFU_REORDER_ID_W = 0,
        CFU_REQ_RESP_ID_W = 0,
        CFU_INPUTS = 2,
        CFU_INPUT_DATA_W = 32,
        CFU_OUTPUTS = 1,
        CFU_OUTPUT_DATA_W = 32,
        CFU_FLOW_REQ_READY_ALWAYS = false,
        CFU_FLOW_RESP_READY_ALWAYS = false,
        CFU_WITH_STATUS = true,
        CFU_RAW_INSN_W = 32,
        CFU_CFU_ID_W = 4,
        CFU_STATE_INDEX_NUM = 5
      ),
      sdramLayout = IS42x320D.layout,
      sdramTimings = IS42x320D.timingGrade7,
      uartCtrlConfig = UartCtrlMemoryMappedConfig(
        uartCtrlConfig = UartCtrlGenerics(
          dataWidthMax      = 8,
          clockDividerWidth = 20,
          preSamplingSize   = 1,
          samplingSize      = 5,
          postSamplingSize  = 2
        ),
        txFifoDepth = 16,
        rxFifoDepth = 16
      ),
      cpuPlugins = ArrayBuffer(
        new PcManagerSimplePlugin(0x80000000l, false),
        new IBusCachedPlugin(
          resetVector = 0x80000000l,
          prediction = STATIC,
          config = InstructionCacheConfig(
            cacheSize = 8192,
            bytePerLine =32,
            wayCount = 1,
            addressWidth = 32,
            cpuDataWidth = 32,
            memDataWidth = 32,
            catchIllegalAccess = true,
            catchAccessFault = true,
            asyncTagMemory = false,
            twoCycleRam = true,
            twoCycleCache = true
          )
        ),
        new DBusCachedPlugin(
          config = new DataCacheConfig(
            cacheSize         = 4096,
            bytePerLine       = 32,
            wayCount          = 1,
            addressWidth      = 32,
            cpuDataWidth      = 32,
            memDataWidth      = 32,
            catchAccessError  = true,
            catchIllegal      = true,
            catchUnaligned    = true
          ),
          memoryTranslatorPortConfig = null
        ),
        new StaticMemoryTranslatorPlugin(
          ioRange      = _(31 downto 28) === 0xF
        ),
        new DecoderSimplePlugin(
          catchIllegalInstruction = true
        ),
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
          zeroBoot = false
        ),
        new IntAluPlugin,
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = true
        ),
        new FullBarrelShifterPlugin,
        new MulPlugin,
        new DivPlugin,
        new HazardSimplePlugin(
          bypassExecute           = true,
          bypassMemory            = true,
          bypassWriteBack         = true,
          bypassWriteBackBuffer   = true,
          pessimisticUseSrc       = false,
          pessimisticWriteRegFile = false,
          pessimisticAddressMatch = false
        ),
        new BranchPlugin(
          earlyBranch = false,
          catchAddressMisaligned = true
        ),
        new CsrPlugin(
          config = CsrPluginConfig(
            catchIllegalAccess = false,
            mvendorid      = null,
            marchid        = null,
            mimpid         = null,
            mhartid        = null,
            misaExtensionsInit = 66,
            misaAccess     = CsrAccess.NONE,
            mtvecAccess    = CsrAccess.NONE,
            mtvecInit      = 0x80000020l,
            mepcAccess     = CsrAccess.READ_WRITE,
            mscratchGen    = false,
            mcauseAccess   = CsrAccess.READ_ONLY,
            mbadaddrAccess = CsrAccess.READ_ONLY,
            mcycleAccess   = CsrAccess.READ_WRITE,
            minstretAccess = CsrAccess.READ_WRITE,
            ecallGen       = false,
            wfiGenAsWait         = false,
            ucycleAccess   = CsrAccess.READ_ONLY,
            uinstretAccess = CsrAccess.READ_WRITE
          )
        ),
        new FpuPlugin(
          externalFpu = false,
          p = FpuParameter(withDouble = false)
        ),
        new CfuPlugin(
          stageCount = 1,
          allowZeroLatency = true,
          encodings = List(
            CfuPluginEncoding(
              instruction = M"-------------------------0001011",
              functionId = List(14 downto 12),
              input2Kind = CfuPlugin.Input2Kind.RS
            )
          ),
          busParameter = CfuBusParameter(
            CFU_VERSION = 0,
            CFU_INTERFACE_ID_W = 0,
            CFU_FUNCTION_ID_W = 3,
            CFU_REORDER_ID_W = 0,
            CFU_REQ_RESP_ID_W = 0,
            CFU_INPUTS = 2,
            CFU_INPUT_DATA_W = 32,
            CFU_OUTPUTS = 1,
            CFU_OUTPUT_DATA_W = 32,
            CFU_FLOW_REQ_READY_ALWAYS = false,
            CFU_FLOW_RESP_READY_ALWAYS = false,
            CFU_WITH_STATUS = true,
            CFU_RAW_INSN_W = 32,
            CFU_CFU_ID_W = 4,
            CFU_STATE_INDEX_NUM = 5
          ),
          enableInit = true
        ),
        new YamlPlugin("cpu0.yaml")
      )
    )
    config
  }
}

class ModRecSoc(val config: ModRecSocConfig) extends Component {

  setDefinitionName("ModRecSoc")

  //Legacy constructor
  def this(axiFrequency: HertzNumber) {
    this(ModRecSocConfig.default.copy(axiFrequency = axiFrequency))
  }

  import config._
  val debug = true
  val interruptCount = 4

  val numChips = 2
  val numPhys = 1

  val io = new Bundle{
    // Clocks / reset
    val asyncReset  = in Bool()
    val axiClk      = in Bool()

    val accelClk    = in Bool()
    val accelReset  = in Bool()

    // Main components IO
    val jtag        = slave(Jtag())

    // Peripherals IO
    val gpioA       = master(TriStateArray(32 bits))
    val gpioB       = master(TriStateArray(32 bits))
//    val gpioAccel   = master(TriStateArray(8 bits))
    val uart        = master(Uart())
    
    val coreInterrupt = in Bool()
    val testMode = in Bool()
    val scan = slave(ScanBus(ScanBusConfig(4, true)))
    val chainSelEn = in Bool()
    val accelClockGenTest = out Bool()

//    val accelCtrlSignals = new Bundle {
//      val arst = in Bool()
//      val start = in Bool()
//      val done  = out Bool()
//    }

    // Phy links
//    val hyper_cs_no: Vec[Bits] = out Vec(Bits(numChips bits), numPhys)
//    val hyper_ck_o = out Bits(numPhys bits)
//    val hyper_ck_no = out Bits(numPhys bits)
//    val hyper_rwds_o = out Bits(numPhys b its)
//    val hyper_rwds_i = in Bits(numPhys bits)
//    val hyper_rwds_oe_o = out Bits(numPhys bits)
//    val hyper_dq_i: Vec[Bits] = in Vec(Bits(8 bits), numPhys)
//    val hyper_dq_o: Vec[Bits] = out Vec(Bits(8 bits), numPhys)
//    val hyper_dq_oe_o = out Bits(numPhys bits)
//    val hyper_reset_no = out Bits(numPhys bits)
  }

  val iBusDebug = new Bundle{
    val isValid = Bool()
    val isStuck = Bool()
    val pc = UInt(32 bits)
    val data = Bits(32 bits)
  }

  val regFileDebug = new Bundle{
    val regFileReadAddress1 = UInt(5 bits)
    val regFileReadAddress2 = UInt(5 bits)
    val rs1Data = Bits(32 bits)
    val rs2Data = Bits(32 bits)
    val regFileWriteValid = Bool()
    val regFileWriteAddress = UInt(5 bits)
    val regFileWriteData = Bits(32 bits)
  }

  val resetCtrlClockDomain = ClockDomain(
    clock = io.axiClk,
    config = ClockDomainConfig(
      resetKind = BOOT
    )
  )

  val resetCtrl = new ClockingArea(resetCtrlClockDomain) {
    val systemResetUnbuffered  = False

    // Implement an counter to keep the reset axiResetOrder high 64 cycles
    // Also this counter will automatically do a reset when the system boots.
    val systemResetCounter = Reg(UInt(6 bits)) init(0)
    when(systemResetCounter =/= U(systemResetCounter.range -> true)){
      systemResetCounter := systemResetCounter + 1
      systemResetUnbuffered := True
    }
    when(BufferCC(io.asyncReset)){
      systemResetCounter := 0
    }

    // Create all reset used later in the design
    val systemReset  = RegNext(systemResetUnbuffered)
    val axiReset     = RegNext(systemResetUnbuffered)
  }

  val axiClockDomain = ClockDomain(
    clock = io.axiClk,
    reset = resetCtrl.axiReset,
    frequency = FixedFrequency(axiFrequency) // The frequency information is used by the SDRAM controller
  )

  val debugClockDomain = ClockDomain(
    clock = io.axiClk,
    reset = resetCtrl.systemReset,
    frequency = FixedFrequency(axiFrequency)
  )

  val axi = new ClockingArea(axiClockDomain) {

    val ram = Axi4CustomOnChipRamWithScan(
      dataWidth = 32,
      byteCount = onChipRamSize,
      idWidth = 4
    )
    println("On chip ram size")
    println(onChipRamSize)

    val sharedRam = Axi4CustomSharedOnChipRam(
      dataWidth = 32,
      byteCount = sscaAccelParams.sharedOnChipRamSize + 0x80000L, // extra bit for fpgaBRAM addr space
      idWidth = 4,
      arwStage = true
    )

    print(s"SharedRAMSize: ${scala.math.pow(2,log2Up(48)).toInt} $sscaAccelParams.sharedOnChipRamSize")

    val sharedMem = SharedOnChipMem(
      dataWidth = 32,
      sscaAccelParams = config.sscaAccelParams
    )

//    val hyperRam = new Hyperbus(
//      // TODO parameter fix
//      // Matches fpga synth
//      numChips = numChips,
//      numPhys = numPhys,
//      isClockODelayed = 0,
//      // Just matched apbBridge
//      axiAddrWidth = 29,
//      axiDataWidth = 32,
//      apbAddrWidth = 19,
//      axiIdWidth = 4,
//      axiUserWidth = 1, // TODO Unsure
//      // TODO regdata handling
//      regAddrWidth = 32,
//      regDataWidth = 32
//    )



    val apbBridge = Axi4SharedToApb3Bridge(
      addressWidth = 20,
      dataWidth    = 32,
      idWidth      = 4
    )

    val gpioACtrl = Apb3Gpio(
      gpioWidth = 32,
      withReadSync = true
    )
    val gpioBCtrl = Apb3Gpio(
      gpioWidth = 32,
      withReadSync = true
    )

    val timerCtrl = new MuraxApb3Timer()

    val uartCtrl = Apb3UartCtrl(uartCtrlConfig)
    uartCtrl.io.apb.addAttribute(Verilator.public)

    // DMA
    val dma = new DmaUnit()

    // CFU accelerators
    val cfuAccels = CfuAccels(config)

    // VexRiscv Core
    val core = new Area{
      val config = VexRiscvConfig(
        plugins = cpuPlugins += new DebugPlugin(debugClockDomain)
      )

      val cpu = new VexRiscv(config)
      var iBus : Axi4ReadOnly = null
      var dBus : Axi4Shared = null
      var cfuBus : CfuBus = null

      for(plugin <- config.plugins) plugin match{
        case plugin : IBusSimplePlugin => iBus = plugin.iBus.toAxi4ReadOnly()
        case plugin : IBusCachedPlugin => {
          iBus = plugin.iBus.toAxi4ReadOnly()
          iBusDebug.isValid := plugin.iBusDebugIsValid.pull()
          iBusDebug.isStuck := plugin.iBusDebugIsStuck.pull()
          iBusDebug.pc := plugin.iBusDebugPc.pull()
          iBusDebug.data := plugin.iBusDebugData.pull()
        }
        case plugin : DBusSimplePlugin => dBus = plugin.dBus.toAxi4Shared()
        case plugin : DBusCachedPlugin => dBus = plugin.dBus.toAxi4Shared(false)
        case plugin : CsrPlugin        => {
          plugin.externalInterrupt := BufferCC(io.coreInterrupt)
          plugin.timerInterrupt := timerCtrl.io.interrupt
        }
        case plugin : DebugPlugin      => debugClockDomain{
          resetCtrl.axiReset setWhen(RegNext(plugin.io.resetOut))
          io.jtag <> plugin.io.bus.fromJtag()
        }
        case plugin:  CfuPlugin => cfuBus = plugin.bus
        case plugin:  RegFilePlugin => {
          regFileDebug.regFileReadAddress1 := plugin.dbgReg_readAddress1.pull()
          regFileDebug.regFileReadAddress2 := plugin.dbgReg_readAddress2.pull()
          regFileDebug.rs1Data := plugin.dbgReg_readRs1Data.pull()
          regFileDebug.rs2Data := plugin.dbgReg_readRs2Data.pull()
          regFileDebug.regFileWriteValid := plugin.dbgReg_writeValid.pull()
          regFileDebug.regFileWriteAddress := plugin.dbgReg_writeAddress.pull()
          regFileDebug.regFileWriteData := plugin.dbgReg_writeData.pull()
        }
        case _ =>
      }
    }

    // AXI4 Crossbar
    val axiCrossbar = Axi4CrossbarFactory()

    axiCrossbar.addSlaves(
      ram.io.axi       -> (0x80000000L,   onChipRamSize),
      //sdramCtrl.io.axi -> (0x40000000L,   sdramLayout.capacity),
      apbBridge.io.axi -> (0xF0000000L,   512 KiB), // 19-bits
      sharedRam.io.axi -> (0xF0080000L,   sscaAccelParams.sharedOnChipRamSize)
      // TODO hyperram uses an axi4 not an axi4 shared which may cause issues?
//      hyperRam.io.axi -> (0xF0080000L + sscaAccelParams.sharedOnChipRamSize, 128 MiB) // TODO Fix based on capacity plan
    )

    axiCrossbar.addConnections(
      core.iBus         -> List(ram.io.axi),
      core.dBus         -> List(ram.io.axi, apbBridge.io.axi, sharedRam.io.axi),
      dma.io.axi_master -> List(ram.io.axi, apbBridge.io.axi, sharedRam.io.axi)
//      cfuAccels.io.dlaMAXI    -> List(hyperRam.io.axi)

    )


    axiCrossbar.addPipelining(apbBridge.io.axi)((crossbar,bridge) => {
      crossbar.sharedCmd.halfPipe() >> bridge.sharedCmd
      crossbar.writeData.halfPipe() >> bridge.writeData
      crossbar.writeRsp             << bridge.writeRsp
      crossbar.readRsp              << bridge.readRsp
    })

    //axiCrossbar.addPipelining(sdramCtrl.io.axi)((crossbar,ctrl) => {
    //  crossbar.sharedCmd.halfPipe()  >>  ctrl.sharedCmd
    //  crossbar.writeData            >/-> ctrl.writeData
    //  crossbar.writeRsp              <<  ctrl.writeRsp
    //  crossbar.readRsp               <<  ctrl.readRsp
    //})

    axiCrossbar.addPipelining(ram.io.axi)((crossbar,ctrl) => {
      //crossbar.sharedCmd.halfPipe()  >>  ctrl.sharedCmd
      crossbar.sharedCmd            >/->  ctrl.sharedCmd
      crossbar.writeData            >/-> ctrl.writeData
      crossbar.writeRsp              <<  ctrl.writeRsp
      crossbar.readRsp               <<  ctrl.readRsp
    })

    axiCrossbar.addPipelining(core.dBus)((cpu,crossbar) => {
      cpu.sharedCmd             >>  crossbar.sharedCmd
      cpu.writeData             >>  crossbar.writeData
      cpu.writeRsp              <<  crossbar.writeRsp
      cpu.readRsp               <-< crossbar.readRsp //Data cache directly use read responses without buffering, so pipeline it for FMax
    })

    axiCrossbar.build()

    println("DEBUG HYPERRAM")
    println(apbBridge.io.apb.config.addressWidth)
//    println(hyperRam.io.apb.config.addressWidth)

    val apbDecoder = Apb3Decoder(
      master = apbBridge.io.apb,
      slaves = List(
        gpioACtrl.io.apb -> (0x00000, 4 kB),
        gpioBCtrl.io.apb -> (0x01000, 4 kB),
        uartCtrl.io.apb  -> (0x10000, 4 kB),
//        hyperRam.io.apb  -> (0x11000, 4 kB),
        timerCtrl.io.apb -> (0x20000, 4 kB)

      )
    )



    // Core/CFU/DMA/DLA/Mem Connections
//    io.gpioAccel <> cfuAccels.io.gpioAccel
    io.accelClockGenTest := cfuAccels.io.accelClockGenTest
    dma.io.writeAddr := cfuAccels.cfuController.io.dmaCtrl.writeAddr.pull()
    dma.io.readAddr := cfuAccels.cfuController.io.dmaCtrl.readAddr.pull()
    dma.io.wordCount := cfuAccels.cfuController.io.dmaCtrl.wordCount.pull()
    dma.io.fireJob := cfuAccels.cfuController.io.dmaCtrl.fireJob.pull()
    cfuAccels.io.dmaJobDone := dma.io.jobDone
    cfuAccels.io.dmaIrq := dma.io.irq


//    dla.io.cfu.cmd.ready := cfuAccels.dlaAccelCmdStream.ready.pull()
//    dla.io.cfu.rsp
//    dla.io.

    cfuAccels.io.cfu <> core.cfuBus
    cfuAccels.io.axiClk := io.axiClk
    cfuAccels.io.axiReset := resetCtrl.axiReset
    cfuAccels.io.accelClk := io.accelClk
    cfuAccels.io.accelReset := io.accelReset
//    cfuAccels.io.accelCtrl.arst := io.accelCtrlSignals.arst
//    cfuAccels.io.accelCtrl.start := io.accelCtrlSignals.start
//    io.accelCtrlSignals.done := cfuAccels.io.accelCtrl.done

//    sharedMem.io.planMemAccelAccessEn := cfuAccels.io.planMemAccessEn
    sharedMem.io.dataMemAccelAccessEn := cfuAccels.io.dataMemAccessEn

    // Shared Memory Connections
//    cfuAccels.io.planMemBus >> sharedMem.io.planMemBus
//    cfuAccels.io.inputMemBus >> sharedMem.io.inputMemBus
//    cfuAccels.io.constMemBus.zipWithIndex foreach { case (constBankBus, i) =>
//      constBankBus >> sharedMem.io.constMemBus(i)
//    }
//    cfuAccels.io.dataMemBuses.zipWithIndex foreach { case (dataBanksBus, i) =>
//      dataBanksBus.zipWithIndex foreach { case (bankBus, j) =>
//        bankBus >> sharedMem.io.dataMemBuses(i)(j)
//      }
//    }

    sharedMem.io.frostMemBus.rdAddr := cfuAccels.io.frostMem.rdAddr
    sharedMem.io.frostMemBus.readEnable := cfuAccels.io.frostMem.readEnable
    sharedMem.io.frostMemBus.wrAddr := cfuAccels.io.frostMem.wrAddr
    sharedMem.io.frostMemBus.writeEnable := cfuAccels.io.frostMem.writeEnable
    sharedMem.io.frostMemBus.wrData := cfuAccels.io.frostMem.wrData
    cfuAccels.io.frostMem.rdData := sharedMem.io.frostMemBus.rdData

    sharedMem.io.shaMemBus.rdAddr := cfuAccels.io.shaMem.rdAddr
    sharedMem.io.shaMemBus.readEnable := cfuAccels.io.shaMem.readEnable
    sharedMem.io.shaMemBus.wrAddr := cfuAccels.io.shaMem.wrAddr
    sharedMem.io.shaMemBus.writeEnable := cfuAccels.io.shaMem.writeEnable
    sharedMem.io.shaMemBus.wrData := cfuAccels.io.shaMem.wrData
    cfuAccels.io.shaMem.rdData := sharedMem.io.shaMemBus.rdData

    // Connect shared OnChip AXI controller to shared Memory inside CFU
    sharedRam.io.sharedMemPort >> sharedMem.io.mem
    sharedMem.io.axiClk := io.axiClk
    sharedMem.io.axiReset := resetCtrl.axiReset
    sharedMem.io.accelClk := cfuAccels.accelClkMux.pull()
    sharedMem.io.accelReset := io.accelReset

    // Hyperram connections
//    hyperRam.io.clk_sys_i := io.axiClk
//    hyperRam.io.rst_sys_ni := ~io.asyncReset
//    hyperRam.io.test_mode_i := False
//
//    io.hyper_cs_no := hyperRam.io.hyper_cs_no
//    io.hyper_ck_o := hyperRam.io.hyper_ck_o
//    io.hyper_ck_no := hyperRam.io.hyper_ck_no
//    io.hyper_rwds_o := hyperRam.io.hyper_rwds_o
//    hyperRam.io.hyper_rwds_i := io.hyper_rwds_i
//    io.hyper_rwds_oe_o := hyperRam.io.hyper_rwds_oe_o
//    hyperRam.io.hyper_dq_i := io.hyper_dq_i
//    io.hyper_dq_o := hyperRam.io.hyper_dq_o
//    io.hyper_dq_oe_o := hyperRam.io.hyper_dq_oe_o
//    io.hyper_reset_no := hyperRam.io.hyper_reset_no
  } // End axiClockDomain

  // Extra scan stuff
  val cpuDebugScanReadReg = ScanReg(ScanRegConfig(ModRecSocConfig.defaultScanBusConfig,
    name = "cpuDebug_rd" ,
    len = 180,
    withCapture = true
  ))

  cpuDebugScanReadReg.io.dataIn.get.addTag(crossClockDomain) := Cat(B(0, 2 bits),
    iBusDebug.isValid.asBits,
    iBusDebug.isStuck.asBits,
    iBusDebug.pc.asBits,
    iBusDebug.data,
    regFileDebug.regFileReadAddress1.asBits,
    regFileDebug.regFileReadAddress2.asBits,
    regFileDebug.rs1Data,
    regFileDebug.rs2Data,
    regFileDebug.regFileWriteValid.asBits,
    regFileDebug.regFileWriteAddress.asBits,
    regFileDebug.regFileWriteData)

  val dmaCtrlScanReg = ScanReg(ScanRegConfig(vexriscv.ip.modrec.ModRecSocConfig.defaultScanBusConfig,
    name = "dmaCtrlDebug_rd",
    len = 92,
    withCapture = true
  ))
  dmaCtrlScanReg.io.dataIn.get.addTag(crossClockDomain) := Cat(B(0, 2 bits),
    axi.dma.io.readAddr.asBits,
    axi.dma.io.writeAddr.asBits,
    axi.dma.io.wordCount.asBits,
    axi.dma.io.fireJob.asBits,
    axi.dma.io.jobDone.asBits)

  val fpgaDebugScanReadReg = ScanReg(ScanRegConfig(vexriscv.ip.modrec.ModRecSocConfig.defaultScanBusConfig,
    name = "fpgaDebug_rd" ,
    len = 512,
    withCapture = true
  ))

// TODO: FPGA fix
  fpgaDebugScanReadReg.io.dataIn.get.addTag(crossClockDomain) := Cat(B(0, 1 bits)
//    Cat((0 until 12).map( i=> axi.cfuAccels.sscaAccel.io.scanOutDbg(i).pull())),
//    axi.cfuAccels.sscaAccel.io.accelCtrl.ctrlIns.pull(),
//    axi.cfuAccels.sscaAccel.io.accelCtrl.statusOuts.pull(),
//    axi.cfuAccels.sscaAccel.io.accelCtrl.start.pull(),
//    axi.cfuAccels.sscaAccel.io.accelCtrl.done.pull(),
//    axi.cfuAccels.sscaAccel.io.accelCtrl.planStartAddr.pull(),
//    axi.cfuAccels.sscaAccel.io.cfu.cmd.payload.raw_insn.pull(),
//    axi.cfuAccels.sscaAccel.io.cfu.cmd.payload.inputs(0).pull(),
//    axi.cfuAccels.sscaAccel.io.cfu.cmd.payload.inputs(1).pull(),
//    axi.cfuAccels.sscaAccel.io.cfu.cmd.ready.pull(),
//    axi.cfuAccels.sscaAccel.io.cfu.cmd.valid.pull(),
//    axi.cfuAccels.sscaAccel.io.cfu.rsp.payload.outputs(0).pull(),
//    axi.cfuAccels.sscaAccel.io.cfu.rsp.ready.pull(),
//    axi.cfuAccels.sscaAccel.io.cfu.rsp.valid.pull()
//    axi.cfuAccels.sscaAccel.io.gpio.writeEnable.pull(),
//    axi.cfuAccels.sscaAccel.io.gpio.write.pull(),
//    axi.cfuAccels.sscaAccel.io.gpio.read.pull()
    ).resized
//  fpgaDebugScanReadReg.io.dataIn.get.addTag(crossClockDomain) := Cat(
//    axi.cfuAccel.cfuManager.io.fpgaCtrl.ctrlIO.pull(),
//    axi.cfuAccel.cfuManager.io.fpgaCtrl.statusIO.pull(),
//    axi.cfuAccel.cfuManager.io.fpgaCtrl.startConfig.pull(),
//    axi.cfuAccel.cfuManager.io.fpgaCtrl.doneConfig.pull(),
//    axi.cfuAccel.cfuManager.io.fpgaCtrl.confStartAddr.pull()
//  ).resized

  val sharedMemDebugScanReadReg = ScanReg(ScanRegConfig(ModRecSocConfig.defaultScanBusConfig,
    name = "sharedMemDebug_rd" ,
    len = 92,
    withCapture = true
  ))

  sharedMemDebugScanReadReg.io.dataIn.get.addTag(crossClockDomain) := Cat(axi.sharedMem.io.mem.cmd.payload.address,
    axi.sharedMem.io.mem.cmd.payload.data,
    axi.sharedMem.io.mem.cmd.payload.mask,
    axi.sharedMem.io.mem.cmd.payload.write,
    axi.sharedMem.io.mem.cmd.ready,
    axi.sharedMem.io.mem.cmd.valid,
    axi.sharedMem.io.mem.rsp.payload.data,
    axi.sharedMem.io.mem.rsp.ready,
    axi.sharedMem.io.mem.rsp.valid).resized


  io.gpioA          <> axi.gpioACtrl.io.gpio
  io.gpioB          <> axi.gpioBCtrl.io.gpio
  io.uart           <> axi.uartCtrl.io.uart

  // Scan controller
  axi.ram.io.testMode := io.testMode

  val scanCtrl = ScanChainCtrl(ModRecSocConfig.defaultScanBusConfig, 6)
  scanCtrl.io.scanCtrlEn := io.chainSelEn
  scanCtrl.io.scan << io.scan

  axi.ram.io.scanBusVec(0) << io.scan
  axi.ram.io.scanBusVec(1) << io.scan
  cpuDebugScanReadReg.io.scanBus << io.scan
  dmaCtrlScanReg.io.scanBus << io.scan
  fpgaDebugScanReadReg.io.scanBus << io.scan
  sharedMemDebugScanReadReg.io.scanBus << io.scan

  io.scan.dataOut.payload := MuxOH(scanCtrl.io.valids, Seq(
    axi.ram.io.scanBusVec(0).dataOut.payload,
    axi.ram.io.scanBusVec(1).dataOut.payload,
    cpuDebugScanReadReg.io.scanBus.dataOut.payload,
    dmaCtrlScanReg.io.scanBus.dataOut.payload,
    fpgaDebugScanReadReg.io.scanBus.dataOut.payload,
    sharedMemDebugScanReadReg.io.scanBus.dataOut.payload
  ))
  io.scan.dataOut.valid := MuxOH(scanCtrl.io.valids, Seq(
    axi.ram.io.scanBusVec(0).dataOut.valid,
    axi.ram.io.scanBusVec(1).dataOut.valid,
    cpuDebugScanReadReg.io.scanBus.dataOut.valid,
    dmaCtrlScanReg.io.scanBus.dataOut.valid,
    fpgaDebugScanReadReg.io.scanBus.dataOut.valid,
    sharedMemDebugScanReadReg.io.scanBus.dataOut.valid
  ))
  //axi.ram.io.scanBusVec(0).dataIn.valid := scanCtrl.io.valids(0)
  //axi.ram.io.scanBusVec(1).dataIn.valid := scanCtrl.io.valids(1)

  // Drive Scan enables
  // Mux scan _outs
}

object ModRecSoc{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    config.addStandardMemBlackboxing(blackboxOnlyIfRequested)
    config.generateVerilog({
      val toplevel = new ModRecSoc(ModRecSocConfig.default)
      toplevel
    })
  }
}



