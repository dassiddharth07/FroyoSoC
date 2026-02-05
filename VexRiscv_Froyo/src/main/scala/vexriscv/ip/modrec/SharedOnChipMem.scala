package vexriscv.ip.modrec

import spinal.core._
import spinal.lib._
import vexriscv.ip.tech.{Mem1R1W, SPMem}

case class Mem1rwCmdBus(dataWidth: Int, addressWidth: Int, maskWidth: Int = -1) extends Bundle {
  def useMask = maskWidth >= 0
  val address = UInt(addressWidth bits)
  val data = Bits(dataWidth bits)
  val write = Bool()
  val mask = ifGen(useMask)(Bits(maskWidth bits))
}

case class MemReadCmdBus(dataWidth: Int, addressWidth: Int) extends Bundle {
  val address = UInt(addressWidth bits)
}

case class MemWriteCmdBus(dataWidth: Int, addressWidth: Int, maskWidth: Int = -1) extends Bundle {
  def useMask = maskWidth >= 0
  val address = UInt(addressWidth bits)
  val data = Bits(dataWidth bits)
  val mask = ifGen(useMask)(Bits(maskWidth bits))
}

case class MemRspBus(dataWidth: Int) extends Bundle {
  val data = Bits(dataWidth bits)
}

case class MemBus(dataWidth: Int, addressWidth: Int, maskWidth: Int = -1) extends Bundle with IMasterSlave {
  val cmd = Stream(Mem1rwCmdBus(dataWidth, addressWidth, maskWidth))
  val rsp = Stream(MemRspBus(dataWidth))

  override def asMaster(): Unit = {
    master(cmd)
    slave(rsp)
  }

  def <<(that: MemBus): Unit = that >> this
  def >>(that: MemBus): Unit = {
    this.cmd >> that.cmd
    this.rsp << that.rsp
  }
}

case class Mem2PortBus(dataWidth: Int, addressWidth: Int, maskWidth: Int = -1) extends Bundle with IMasterSlave {
  val cmdRead = Stream(MemReadCmdBus(dataWidth, addressWidth))
  val cmdWrite = Stream(MemWriteCmdBus(dataWidth, addressWidth, maskWidth))
  val rsp = Stream(MemRspBus(dataWidth))

  override def asMaster(): Unit = {
    master(cmdRead)
    master(cmdWrite)
    slave(rsp)
  }

  def <<(that: Mem2PortBus): Unit = that >> this
  def >>(that: Mem2PortBus): Unit = {
    this.cmdRead >> that.cmdRead
    this.cmdWrite >> that.cmdWrite
    this.rsp << that.rsp
  }
}

case class SharedOnChipMem(dataWidth : Int, sscaAccelParams: SscaAccelParams) extends Component {

  val inputMemBankSize = sscaAccelParams.inputMemBankSize
  val inputMemPerBankWordCount = inputMemBankSize / 4
  val inputMemWordCount = (inputMemPerBankWordCount + 64) * 2 // Double buffered input mem. need extra 64 words for SSCA input.

  val dataMemBankSize = sscaAccelParams.dataMemBankSize
  val dataMemPerBankWordCount = dataMemBankSize / 4
  val dataMemBanks = sscaAccelParams.dataMemBanks
  val dataMemGroups = sscaAccelParams.dataMemGroups
  val dataMemTotalWordCount = dataMemPerBankWordCount * dataMemBanks * dataMemGroups

  val planMemWordCount = sscaAccelParams.planMemSize / 4

  val constMemPerBankWordCount = sscaAccelParams.constMemBankSize / 4

  val maskWidth = dataWidth / 8
  val memWordAddrWidth = log2Up(inputMemWordCount + planMemWordCount + dataMemTotalWordCount + 0x20000L) // +1 for BRAM address space

  val io = new Bundle {
    val accelClk = in Bool()
    val accelReset = in Bool()
    val axiClk = in Bool()
    val axiReset = in Bool()

    // AXI4 side signals
    val mem = slave(MemBus(dataWidth, memWordAddrWidth, maskWidth))

    // CFU Accel side signals
//    val inputMemBus = slave(MemBus(dataWidth, log2Up(inputMemWordCount)))
//    val planMemBus = slave(MemBus(dataWidth, log2Up(planMemWordCount)))
//    val dataMemBuses = Vec(Vec(slave(Mem2PortBus(dataWidth, log2Up(dataMemPerBankWordCount))), dataMemBanks), dataMemGroups)
//    val constMemBus = Vec(slave(MemBus(dataWidth, log2Up(constMemPerBankWordCount))), dataMemBanks)

//    val planMemAccelAccessEn = in Bool()
    val dataMemAccelAccessEn = in Bits(2 bits)

    // Frost Accel
    val frostMemBus = new Bundle {
      val rdAddr = in Bits (9 bits)
      val wrAddr = in Bits (9 bits)
      val rdData = out Bits (512 bits)
      val readEnable = in Bool()
      val writeEnable = in Bool()
      val wrData = in Bits (512 bits)
    }

    // SHA Accel
    val shaMemBus = new Bundle {
      val rdAddr = in Bits (9 bits)
      val wrAddr = in Bits (9 bits)
      val rdData = out Bits (512 bits)
      val readEnable = in Bool()
      val writeEnable = in Bool()
      val wrData = in Bits (512 bits)
    }
  }

  val axiClk = ClockDomain(io.axiClk, io.axiReset)
  val accelClk = ClockDomain(io.accelClk, io.accelReset)
  // lower bit selects between cpu (X0) and accels (X1). upper bit selects between frost (11) /ssca (01)
  val memClk = Mux(io.dataMemAccelAccessEn(0), io.accelClk, io.axiClk) // Use technology specific clock mux

  val accelDataMemList = Seq.fill(dataMemGroups)(Range(0, dataMemBanks))
  val accelDataMems = accelDataMemList.zipWithIndex map {case (group, i) =>
    group.zipWithIndex map { case (bank, j) =>
      if(i == 0 && j == 0) {
        Mem1R1W(dataWidth, dataMemPerBankWordCount, true, ModRecSocConfig.tech)
      } else {
        Mem1R1W(dataWidth, dataMemPerBankWordCount, false, ModRecSocConfig.tech)
      }
    }
  }





  val axiArea = new ClockingArea(axiClk) {
    // Connect axi membus to rams ADDR MAP [ READ BRAM (4K), WRITE BRAM (4K), MEM 0-4 (80K)]
    println(log2Up(dataMemTotalWordCount))
    println(io.mem.cmd.address.getWidth)
    val memAddrSel = io.mem.cmd.address - planMemWordCount - 0x20000L
    val wordAddress = io.mem.cmd.address - 0x20000L
    val isDataMemAddr = (io.mem.cmd.address - 0x20000L) >= planMemWordCount

//    val inputMemsEn: Seq[Bool] = Range(0, 2).map(i =>
//      isDataMemAddr && (memAddrSel >= i * (inputMemPerBankWordCount+64)) && (memAddrSel < (i + 1) * (inputMemPerBankWordCount+64))
//    )
//    val planMemEn = !isDataMemAddr // incr 4 -> 4*4 KB
    val dataMemsEn: Seq[Seq[Bool]] = Range(0, dataMemGroups).map(j =>
      Range(0, dataMemBanks).map(i =>
        isDataMemAddr && (memAddrSel >= (inputMemWordCount + i * dataMemPerBankWordCount + j * dataMemPerBankWordCount * dataMemBanks)) &&
          (memAddrSel < (inputMemWordCount + (i + 1) * dataMemPerBankWordCount + j * dataMemPerBankWordCount * dataMemBanks)) // incr 4 -> 4*4 KB
      )
    )

    val dataMemsEnFFs = dataMemsEn.map(ramsEn => ramsEn.map(ramEn => (RegNextWhen(ramEn, io.mem.cmd.valid && !io.mem.cmd.write))))
//    val constMemEnFFs = constMemEn.map(memEn => RegNextWhen(memEn, io.mem.cmd.valid && !io.mem.cmd.write))
    // TEST: setup negative slack without pipe regs?

  } // End axiClk area
    val isSHAAccessEn = io.dataMemAccelAccessEn === B"01" // "01": SHA
    val isAccelAccessEn = io.dataMemAccelAccessEn(0) // "X1": Frost/SSCA Accels

  io.mem.cmd.ready := True

  // TEST: setup negative slack without pipe regs?
  val dataMemsRspWire = Vector.fill(dataMemGroups)( Vector.fill(dataMemBanks)( (Bits(32 bits)) ) )
  dataMemsRspWire.zipWithIndex.foreach { case (memBanksWires, i) =>
    memBanksWires.zipWithIndex.foreach { case (bankWire, j) =>
      bankWire := accelDataMems(i)(j).io.rd_data
    }
  }

  val dataMemsRspRegs = Vector.fill(dataMemGroups)( Vector.fill(dataMemBanks)( Reg(Bits(32 bits)) ) )
  dataMemsRspRegs.zipWithIndex.foreach { case (memBanksRegs, i) =>
    memBanksRegs.zipWithIndex.foreach { case (bankReg, j) =>
      bankReg := dataMemsRspWire(i)(j)
    }
  }
  // Delay select bit for data mem request
  val dataMemsRspData = dataMemsRspWire.map(ramBanksWires => ramBanksWires.map (bankWires =>
                            RegNext(isAccelAccessEn) ? B(0, dataWidth bits) | bankWires))
//  val constMemRspData = constMem.zipWithIndex map {case (bank, i) =>
//    bank.io.out_data
//  }

  // Mem response is valid two cycles later for data mem
  io.mem.rsp.valid := (axiArea.dataMemsEnFFs.flatten).orR
  io.mem.rsp.payload.data := MuxOH((axiArea.dataMemsEnFFs.flatten).asBits,
    dataMemsRspData.flatten)


  // Data Mem Accel connections
  accelDataMems.zipWithIndex.foreach { case (memBanks, i) =>
    memBanks.zipWithIndex.foreach { case (bank, j) =>
      bank.io.rd_clk := memClk
      bank.io.wr_clk := memClk
      if (i == 0 && j == 0) {
        val bankNum = i * dataMemBanks + j
        val accelMux_wren = Mux(io.dataMemAccelAccessEn(1), io.frostMemBus.writeEnable, io.shaMemBus.writeEnable)
        val accelMux_wraddr = Mux(io.dataMemAccelAccessEn(1), io.frostMemBus.wrAddr.resize(log2Up(dataMemPerBankWordCount)).asUInt, io.shaMemBus.wrAddr.resize(log2Up(dataMemPerBankWordCount)).asUInt)
        val accelMux_wrdata = Mux(io.dataMemAccelAccessEn(1), io.frostMemBus.wrData(32 * (bankNum + 1) - 1 downto 32 * bankNum), io.shaMemBus.wrData(32 * (bankNum + 1) - 1 downto 32 * bankNum))

        val accelMux_rden = Mux(io.dataMemAccelAccessEn(1), io.frostMemBus.readEnable, io.shaMemBus.readEnable)
        val accelMux_rdaddr = Mux(io.dataMemAccelAccessEn(1), io.frostMemBus.rdAddr.resize(log2Up(dataMemPerBankWordCount)).asUInt, io.shaMemBus.rdAddr.resize(log2Up(dataMemPerBankWordCount)).asUInt)

        bank.io.wr_en := Mux(io.dataMemAccelAccessEn(0), accelMux_wren, io.mem.cmd.valid && io.mem.cmd.write && axiArea.dataMemsEn(i)(j))
        bank.io.wr_addr := Mux(io.dataMemAccelAccessEn(0), accelMux_wraddr, (axiArea.wordAddress - planMemWordCount - inputMemWordCount).resize(log2Up(dataMemPerBankWordCount)))
        bank.io.wr_mask := B"4'xF"
        bank.io.wr_data := Mux(io.dataMemAccelAccessEn(0), accelMux_wrdata, io.mem.cmd.data)

        val bank_rd_en = Mux(io.dataMemAccelAccessEn(0), accelMux_rden, io.mem.cmd.valid && !io.mem.cmd.write && axiArea.dataMemsEn(i)(j))
        bank.io.rd_en := bank_rd_en
        bank.io.rd_addr := Mux(io.dataMemAccelAccessEn(0), accelMux_rdaddr, (axiArea.wordAddress - planMemWordCount - inputMemWordCount).resize(log2Up(dataMemPerBankWordCount)))

        // Frost
        bank.io.mode := (io.dataMemAccelAccessEn(0))
        bank.io.wrData512 := Mux(io.dataMemAccelAccessEn(1), io.frostMemBus.wrData, io.shaMemBus.wrData)
        io.frostMemBus.rdData := bank.io.rdData512
        io.shaMemBus.rdData := bank.io.rdData512

      }

    }
  }

}

object SharedOnChipMemGen{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    config.addStandardMemBlackboxing(blackboxOnlyIfRequested)
    config.generateVerilog(SharedOnChipMem(32, ModRecSocConfig.default.sscaAccelParams))
  }
}



