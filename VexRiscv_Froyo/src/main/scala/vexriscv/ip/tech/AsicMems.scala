package vexriscv.ip.tech

import spinal.core._



case class DPMemMacro(dataWidth : Int, wordCount: Int, colMux: Int, tech: String) extends BlackBox {
  val io = new Bundle {

    val AA = (tech == "N16") generate (in UInt (log2Up(wordCount) bits))
    val D = (tech == "N16") generate (in Bits (dataWidth bits))
    val BWEB = (tech == "N16") generate (in Bits (dataWidth bits))
    val WEB = (tech == "N16") generate (in Bool())
    val AB = (tech == "N16") generate (in UInt (log2Up(wordCount) bits))
    val REB = (tech == "N16") generate (in Bool())
    val CLK = (tech == "N16") generate (in Bool())
    val Q = (tech == "N16") generate (out Bits (dataWidth bits))
  }

  tech match {
    case "N16" => setDefinitionName(f"DPMEM1024X${dataWidth}M${colMux}_mem")
  }

  noIoPrefix()
}

case class Mem1R1W(dataWidth : Int, wordCount: Int, hasMode: Boolean, tech: String) extends Component {
  val io = new Bundle {
    val wr_clk = in Bool()
    val wr_en = in Bool()
    val wr_addr = in UInt(log2Up(wordCount) bits)
    val wr_mask = in Bits((dataWidth/8) bits)
    val wr_data = in Bits(dataWidth bits)

    val rd_clk = in Bool()
    val rd_en = in Bool()
    val rd_addr = in UInt(log2Up(wordCount) bits)
    val rd_data = out Bits(dataWidth bits)

//    if (hasMode) {
    val mode = hasMode generate (in Bool())
    val wrData512 = hasMode generate (in Bits(dataWidth * 16 bits))
    val rdData512 = hasMode generate (out Bits(dataWidth * 16 bits))
//    }

  }

  val numMacros = wordCount/1024 + (if (wordCount % 1024 != 0) 1 else 0)
  val sram_macros = Seq.fill(numMacros)(DPMemMacro(dataWidth, 1024, colMux=4, tech))
  println(wordCount)

  tech match {

    case "N16" =>
      sram_macros.zipWithIndex foreach { case (sram, i) =>
        sram.io.AA    := io.wr_addr(9 downto 0)
        if(hasMode) {
          sram.io.D     := io.mode ? io.wrData512((16-i)*32-1 downto (15-i)*32).asBits | io.wr_data
          sram.io.WEB   := io.mode ? !io.wr_en | !(io.wr_en && io.wr_addr(log2Up(wordCount)-1 downto 10) === i)
          sram.io.REB   := io.mode ? !io.rd_en | !(io.rd_en && io.rd_addr(log2Up(wordCount)-1 downto 10) === i)

        } else {
          sram.io.D     := io.wr_data
          sram.io.WEB   := !(io.wr_en && io.wr_addr(log2Up(wordCount)-1 downto 10) === i)
          sram.io.REB   := !(io.rd_en && io.rd_addr(log2Up(wordCount)-1 downto 10) === i)
        }


        sram.io.BWEB  := Cat(Seq.fill(8)(!io.wr_mask(3)) ++ Seq.fill(8)(!io.wr_mask(2)) ++
                              Seq.fill(8)(!io.wr_mask(1)) ++ Seq.fill(8)(!io.wr_mask(0))).reversed
        sram.io.AB    := io.rd_addr(9 downto 0)
        sram.io.CLK   := io.rd_clk // read and write clock are the same...

      }

      // Flop upper read address bits for read data mux
      val memClk = ClockDomain(io.rd_clk)
      val rdClkArea = new ClockingArea(memClk) {
        io.rd_data := RegNext(io.rd_addr(log2Up(wordCount)-1 downto 10)).addTag(crossClockDomain).muxList(
          sram_macros.zipWithIndex.map { case (sram, i) =>
            (i, sram.io.Q)
          }
        )
      }
      if(hasMode) {
        io.rdData512 := Cat(sram_macros.reverse map {bank => bank.io.Q})
      }

  }
}

object Mem1R1WGen{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    config.generateVerilog({
      val toplevel = new Mem1R1W(32, (BigInt(2).KiB/4).toInt, false, "N16")
      toplevel
    })
  }
}



case class SPMemMacro(dataWidth : Int, wordCount: Int, colMux: Int, tech: String, macroType: String) extends BlackBox {
  val io = new Bundle {

    // TSMC N16 Macro
    val CLK     = (tech == "N16") generate (in Bool())
    val CEB     = (tech == "N16") generate (in Bool())
    val WEB     = (tech == "N16") generate (in Bool())
    val A       = (tech == "N16") generate (in UInt (log2Up(wordCount) bits))
    val D       = (tech == "N16") generate (in Bits (dataWidth bits))
    val BWEB    = (tech == "N16") generate (in Bits (dataWidth bits))

    val Q       = (tech == "N16") generate (out Bits (dataWidth bits))

  }

  (tech, macroType) match {
    case ("N16", "SHD") => setDefinitionName(f"SPMEM${wordCount}X${dataWidth}M${colMux}_mem")
  }

  noIoPrefix()
}

case class SPMem(dataWidth : Int, wordCount: Int, colMux: Int = 2, tech: String, macroType: String) extends Component {
  val io = new Bundle {
    val clk = in Bool()
    val enable = in Bool()
    val write = in Bool()
    val address = in UInt(log2Up(wordCount) bits)
    val mask = in Bits((dataWidth/8) bits)
    val data = in Bits(dataWidth bits)
    val out_data = out Bits(dataWidth bits)
  }

  val numMacros = wordCount/8192 + (if (wordCount % 8192 != 0) 1 else 0)
  val sram_macros = tech match {
    case "N16" if numMacros == 1 =>
      Seq.fill(1)(SPMemMacro(dataWidth, wordCount, colMux, tech, macroType))
    case "N16" if numMacros > 1 =>
      Seq.fill(numMacros)(SPMemMacro(dataWidth, 8192, colMux, tech, macroType))
  }

//  val sram_macro = SPMemMacro(dataWidth, wordCount, colMux, tech)

  tech match {

    case "N16" if numMacros == 1 =>
      sram_macros(0).io.CLK   := io.clk
      sram_macros(0).io.CEB   := !io.enable
      sram_macros(0).io.WEB   := !io.write
      sram_macros(0).io.A     := io.address
      sram_macros(0).io.BWEB  := Cat(Seq.fill(8)(!io.mask(3)) ++ Seq.fill(8)(!io.mask(2)) ++
                                  Seq.fill(8)(!io.mask(1)) ++ Seq.fill(8)(!io.mask(0))).reversed
      sram_macros(0).io.D     := io.data
      io.out_data             := sram_macros(0).io.Q


    case "N16" if numMacros > 1 =>
      sram_macros.zipWithIndex foreach {case (sram, i) =>
        sram.io.CLK   := io.clk
        sram.io.CEB   := !(io.enable && io.address(log2Up(wordCount)-1 downto 13) === i)
        sram.io.WEB   := !io.write
        sram.io.A     := io.address(12 downto 0)
        sram.io.BWEB  := Cat(Seq.fill(8)(!io.mask(3)) ++ Seq.fill(8)(!io.mask(2)) ++
                              Seq.fill(8)(!io.mask(1)) ++ Seq.fill(8)(!io.mask(0))).reversed
        sram.io.D     := io.data


      }
      io.out_data := RegNext(io.address(log2Up(wordCount)-1 downto 13)).muxListDc(
        sram_macros.zipWithIndex.map { case (sram, i) =>
          (i, sram.io.Q)
        }
      )
  }
}


