package vexriscv.ip.frost

import scala.util.Random

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.amba4.axilite._
import spinal.lib.fsm._

case class parse_scalar() extends Component{
  val io = new Bundle{
    val start = in Bool()
    val done = out Bool()

    val a = in Bits(256 bits)
    val e = out Vec(Bits(5 bits), 64)
  }

  val clk = in Bool()
  val rst = in Bool()

  val y = Bits(6 bits)
  val z = Bits(6 bits)
  val e_wr = Bits(6 bits)
  val e1_wr = Bits(6 bits)
  val e2_wr = Bits(6 bits)
  val e_wsign = Bits(5 bits)


  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {
    val e_reg = Reg(Vec(Bits(5 bits), 64))
    val cnt = Reg(UInt(7 bits))
    val carry = Reg(Bits(4 bits))

    val x = Bits(4 bits)

    e_reg(0) := (cnt === 0 && io.start === True)? io.a(63 downto 60).asBits.resize(5 bits) | ((cnt > 0 && cnt < 65)? e_wsign | e_reg(0))
    for (i <- 1 until 64) {
      e_reg(i) := (cnt === 0 && io.start === True)? io.a(4 * (64-i) - 1 downto 4 * (63-i)).asBits.resize(5 bits) | ((cnt > 0 && cnt < 65)? e_reg(i-1) |  e_reg(i))
    }

//    e_reg(0) := (cnt === 0) ? io.a(63 downto 60).asBits | ((cnt === 65) ? e_reg(0) | e_wr(3 downto 0))
//    for (i <- 1 until 64) {
//      e_reg(i) := (cnt === 0) ? io.a(4 * (64 - i) - 1 downto 4 * (63 - i)).asBits | ((cnt === 65) ? e_reg(i) | e_reg(i - 1))
//    }

    x := e_reg(63)(3 downto 0)
    carry := (cnt === 0)? B(0, 4 bits) | z(5 downto 4).resize(4 bits)

    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntFIFO = new State
      val cntMulOnly = new State

      io.done := False

      cntInit
        .onEntry {
          cnt := U(0, 7 bits)
        }
        .whenIsActive {
          when(io.start === True) {
            goto(cntFIFO)
          }
        }
      cntFIFO
        .onEntry{
          cnt := cnt + 1
//          carry := B(0, 4 bits)
        }
        .whenIsActive {
          cnt := cnt + 1
          when(cnt === 65){
            io.done := True
            goto(cntInit)
          }
        }
    }
  }

  y := (coreArea.x.resize(6 bits).asSInt + coreArea.carry.asSInt.resize(6 bits)).asBits
  z := (coreArea.cnt === U(64, 7 bits))? B(0, 6 bits) | (y.asSInt + S(8, 6 bits)).asBits
  e1_wr := (y.asSInt - Cat(z(5 downto 4), B(0, 4 bits)).asSInt.resize(6 bits)).asBits
  e2_wr := (Cat(z(5 downto 4), B(0, 4 bits)).asSInt.resize(6 bits) - y.asSInt).asBits
  e_wr := (z(5 downto 4) === 0)? e1_wr | e2_wr
  e_wsign := Cat(z(4 downto 4), e_wr(3 downto 0))

//  e_wr := (y.asSInt - Cat(z(5 downto 4), B(0, 4 bits)).asSInt.resize(6 bits)).asBits

  for (i <- 0 until 64) {
    io.e(i) := coreArea.e_reg(63-i)
  }

}

//object ParseScalarGen{
//  def main(args: Array[String]) {
//    val config = SpinalConfig(targetDirectory = "verilog_outputs")
//    config.generateVerilog({
//      val toplevel = new parse_scalar()
//      toplevel
//    })
//  }
//}


case class neg_T2D() extends Component{

  val io = new Bundle{
    val start = in Bool()
    val done = out Bool()

    val subDone = in Bool()
    val fsub_start = out Bool()

    val T2D = in Bits(256 bits)
    val neg_T2D = out Bits(256 bits)

    val sub1_A = out Bits (256 bits)
    val sub1_B = out Bits (256 bits)
    val sub1_OUT = in Bits (256 bits)
  }
  val clk = in Bool()
  val rst = in Bool()

  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {


    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntSub = new State

      io.fsub_start := False

      io.sub1_A := B(0, 256 bits)
      io.sub1_B := B(0, 256 bits)

      io.neg_T2D := B(0, 256 bits)

      io.done := False

      cntInit
        .whenIsActive {
          when(io.start === True) {
            goto(cntSub)
          }
        }
      cntSub
        .onEntry {
          io.fsub_start := True
          io.sub1_A := B(0, 256 bits)
          io.sub1_B := io.T2D
        }
        .whenIsActive {
          when(io.subDone === True) {
            goto(cntInit)
            io.done := True
            io.neg_T2D := io.sub1_OUT
          }
        }


    }
  }



}


case class p3_to_cached() extends Component{

  val io = new Bundle{
    val start = in Bool()
    val done = out Bool()
    val YpluxX_done = out Bool()
    val YminusX_done = out Bool()
    val T2d_done = out Bool()

    val addDone = in Bool()
    val subDone = in Bool()
    val mulDone = in Bool()

    val fadd_start = out Bool()
    val fsub_start = out Bool()
    val fmul_start = out Bool()

    val p_X = in Bits(256 bits)
    val p_Y = in Bits(256 bits)
    val p_Z = in Bits(256 bits)
    val p_T = in Bits(256 bits)

    val d2 = in Bits(256 bits)

    val r_YplusX = out Bits (256 bits)
    val r_YminusX = out Bits (256 bits)
    val r_Z = out Bits (256 bits)
    val r_T2d = out Bits (256 bits)


    val mul1_A = out Bits(256 bits)
    val mul1_B = out Bits(256 bits)
    val mul1_OUT = in Bits(256 bits)

    val add1_A = out Bits (256 bits)
    val add1_B = out Bits (256 bits)
    val add1_OUT = in Bits (256 bits)

    val sub1_A = out Bits (256 bits)
    val sub1_B = out Bits (256 bits)
    val sub1_OUT = in Bits (256 bits)

  }

  io.YpluxX_done := io.addDone
  io.YminusX_done := io.subDone
  io.T2d_done := io.mulDone
//  io.done := io.mulDone

  val clk = in Bool()
  val rst = in Bool()


  io.r_Z := io.p_Z



  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {


    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntAddSubMul = new State
      val cntMulOnly = new State

      io.fadd_start := False
      io.fsub_start := False
      io.fmul_start := False


      io.add1_A := B(0, 256 bits)
      io.add1_B := B(0, 256 bits)
      io.sub1_A := B(0, 256 bits)
      io.sub1_B := B(0, 256 bits)
      io.mul1_A := B(0, 256 bits)
      io.mul1_B := B(0, 256 bits)

      io.r_YplusX := B(0, 256 bits)
      io.r_YminusX := B(0, 256 bits)
      io.r_T2d := B(0, 256 bits)
      io.done := False

      cntInit
        .whenIsActive {
          when(io.start === True) {
            goto(cntAddSubMul)
          }
        }
      cntAddSubMul
        .onEntry {
          io.fadd_start := True
          io.fsub_start := True
          io.fmul_start := True
          io.add1_A := io.p_Y
          io.add1_B := io.p_X
          io.sub1_A := io.p_Y
          io.sub1_B := io.p_X
          io.mul1_A := io.p_T
          io.mul1_B := io.d2
        }
        .whenIsActive {
          when(io.mulDone === True) {
            goto(cntInit)
            io.done := True
            io.r_T2d := io.mul1_OUT
            io.r_YplusX := io.add1_OUT
            io.r_YminusX := io.sub1_OUT
          }
        }

//      cntAddSubMul
//        .onEntry {
//          io.fadd_start := True
//          io.fsub_start := True
//          io.fmul_start := True
//          io.add1_A := io.p_Y
//          io.add1_B := io.p_X
//          io.sub1_A := io.p_Y
//          io.sub1_B := io.p_X
//          io.mul1_A := io.p_T
//          io.mul1_B := io.d2
//        }
//        .whenIsActive {
//          //          io.mul1_A := io.p_T
//          //          io.mul1_B := io.d2
//          goto(cntMulOnly)
//        }
//      cntMulOnly
//        .onEntry {
//          //          io.r_YplusX := io.add1_OUT
//          //          io.r_YminusX := io.sub1_OUT
//        }
//        .whenIsActive {
//          when(io.mulDone === True) {
//            goto(cntInit)
//            io.r_T2d := io.mul1_OUT
//            io.r_YplusX := io.add1_OUT
//            io.r_YminusX := io.sub1_OUT
//          }
//        }
    }
  }
}

case class p1p1_to_p3() extends Component{
  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()

    val mulDone = in Bool()


    val fmul1_start = out Bool()

    val fmul2_start = out Bool()

    val p_X = in Bits (256 bits)
    val p_Y = in Bits (256 bits)
    val p_Z = in Bits (256 bits)
    val p_T = in Bits (256 bits)

    val r_X = out Bits (256 bits)
    val r_Y = out Bits (256 bits)
    val r_Z = out Bits (256 bits)
    val r_T = out Bits (256 bits)

    val mul1_A = out Bits (256 bits)
    val mul1_B = out Bits (256 bits)
    val mul1_OUT = in Bits (256 bits)

    val mul2_A = out Bits (256 bits)
    val mul2_B = out Bits (256 bits)
    val mul2_OUT = in Bits (256 bits)


  }

  val clk = in Bool()
  val rst = in Bool()

  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {

    val r_X = Reg(Bits(256 bits))
    val r_Y = Reg(Bits(256 bits))
    val r_Z = Reg(Bits(256 bits))
    val r_T = Reg(Bits(256 bits))

    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntMul1 = new State
      val cntMul2 = new State
      val cntAddSub1 = new State
      val cntAddSub2 = new State

      io.fmul1_start := False
      io.fmul2_start := False

      io.mul1_A := B(0, 256 bits)
      io.mul1_B := B(0, 256 bits)

      io.mul2_A := B(0, 256 bits)
      io.mul2_B := B(0, 256 bits)

      io.r_X := B(0, 256 bits)
      io.r_Y := B(0, 256 bits)
      io.r_T := B(0, 256 bits)
      io.r_Z := B(0, 256 bits)
      io.done := False

      r_X := r_X
      r_Y := r_Y
      r_T := r_T
      r_Z := r_Z

      cntInit
        .whenIsActive {
          when(io.start === True) {
            goto(cntMul1)
          }
        }
      cntMul1
        .onEntry {
          io.fmul1_start := True
          io.fmul2_start := True
          io.mul1_A := io.p_X
          io.mul1_B := io.p_T
          io.mul2_A := io.p_Y
          io.mul2_B := io.p_Z
        }
        .whenIsActive {
          when(io.mulDone === True) {
            goto(cntMul2)
          }
        }
      cntMul2
        .onEntry {
          io.fmul1_start := True
          io.fmul2_start := True
          io.mul1_A := io.p_Z
          io.mul1_B := io.p_T
          io.mul2_A := io.p_X
          io.mul2_B := io.p_Y
          r_X := io.mul1_OUT
          r_Y := io.mul2_OUT
        }
        .whenIsActive {
          when(io.mulDone === True) {
            io.r_X := r_X
            io.r_Y := r_Y
            io.r_Z := io.mul1_OUT
            io.r_T := io.mul2_OUT
            io.done := True
            goto(cntInit)
          }
        }
    }

  }
}

case class p3_dbl() extends Component{
  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()
//    val X = out Bool()
//    val Y = out Bool()
//    val T = out Bool()
//    val Z = out Bool()

    val addDone = in Bool()
    val subDone = in Bool()
    val mulDone = in Bool()

    val fadd1_start = out Bool()
    val fsub1_start = out Bool()
    val fmul1_start = out Bool()

    val fadd2_start = out Bool()
    val fsub2_start = out Bool()
    val fmul2_start = out Bool()

    val p_X = in Bits (256 bits)
    val p_Y = in Bits (256 bits)
    val p_Z = in Bits (256 bits)
    val p_T = in Bits (256 bits)

    val r_X = out Bits (256 bits)
    val r_Y = out Bits (256 bits)
    val r_Z = out Bits (256 bits)
    val r_T = out Bits (256 bits)


    val mul1_A = out Bits (256 bits)
    val mul1_B = out Bits (256 bits)
    val mul1_OUT = in Bits (256 bits)

    val add1_A = out Bits (256 bits)
    val add1_B = out Bits (256 bits)
    val add1_OUT = in Bits (256 bits)

    val sub1_A = out Bits (256 bits)
    val sub1_B = out Bits (256 bits)
    val sub1_OUT = in Bits (256 bits)

    val mul2_A = out Bits (256 bits)
    val mul2_B = out Bits (256 bits)
    val mul2_OUT = in Bits (256 bits)


  }

  val clk = in Bool()
  val rst = in Bool()

  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {

    val r_X = Reg(Bits(256 bits))
    val r_Y = Reg(Bits(256 bits))
    val r_Z = Reg(Bits(256 bits))
    val r_T = Reg(Bits(256 bits))

    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntAddMul1 = new State
      val cntAddMul2 = new State
      val cntAddSub1 = new State
      val cntAddSub2 = new State

      io.fadd1_start := False
      io.fsub1_start := False
      io.fmul1_start := False
      io.fadd2_start := False
      io.fsub2_start := False
      io.fmul2_start := False

      io.add1_A := B(0, 256 bits)
      io.add1_B := B(0, 256 bits)
      io.sub1_A := B(0, 256 bits)
      io.sub1_B := B(0, 256 bits)
      io.mul1_A := B(0, 256 bits)
      io.mul1_B := B(0, 256 bits)

//      io.add2_A := B(0, 256 bits)
//      io.add2_B := B(0, 256 bits)
//      io.sub2_A := B(0, 256 bits)
//      io.sub2_B := B(0, 256 bits)
      io.mul2_A := B(0, 256 bits)
      io.mul2_B := B(0, 256 bits)

      io.r_X := B(0, 256 bits)
      io.r_Y := B(0, 256 bits)
      io.r_T := B(0, 256 bits)
      io.r_Z := B(0, 256 bits)
      io.done := False

      r_X := r_X
      r_Y := r_Y
      r_T := r_T
      r_Z := r_Z

      cntInit
        .whenIsActive {
          when(io.start === True) {
            goto(cntAddMul1)
          }
        }
      cntAddMul1
        .onEntry {
          io.fadd1_start := True
          io.fmul1_start := True
          io.fmul2_start := True
          io.add1_A := io.p_Y
          io.add1_B := io.p_X
          io.mul1_A := io.p_X
          io.mul1_B := io.p_X
          io.mul2_A := io.p_Y
          io.mul2_B := io.p_Y
          r_Z := io.p_Z
        }
        .whenIsActive {
          when(io.mulDone === True) {
            goto(cntAddMul2)
          }
        }
      cntAddMul2
        .onEntry {
          io.fadd1_start := True
          io.fsub1_start := True
          io.fmul1_start := True
          io.fmul2_start := True
          io.add1_A := io.mul2_OUT
          io.add1_B := io.mul1_OUT
          io.sub1_A := io.mul2_OUT
          io.sub1_B := io.mul1_OUT
          io.mul1_A := r_Z
          io.mul1_B := r_Z
          io.mul2_A := io.add1_OUT
          io.mul2_B := io.add1_OUT
        }
        .whenIsActive {
          when(io.mulDone === True) {
            r_Y := io.add1_OUT
            r_Z := io.sub1_OUT
            goto(cntAddSub1)
          }
        }
      cntAddSub1
        .onEntry {
          io.fadd1_start := True
          io.fsub1_start := True
          io.add1_A := io.mul1_OUT
          io.add1_B := io.mul1_OUT
          io.sub1_A := io.mul2_OUT
          io.sub1_B := io.add1_OUT
        }
        .whenIsActive {
          when(io.addDone === True) {
            r_X := io.sub1_OUT
            r_T := io.add1_OUT
            goto(cntAddSub2)
          }
        }
      cntAddSub2
        .onEntry {
          io.fsub1_start := True
          io.sub1_A := io.add1_OUT
          io.sub1_B := r_Z
        }
        .whenIsActive {
          when(io.subDone === True) {
            io.r_X := r_X
            io.r_Y := r_Y
            io.r_Z := r_Z
            io.r_T := io.sub1_OUT
            io.done := True
            goto(cntInit)
          }
        }
    }

  }
}


case class add_cached() extends Component {
  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()
    //    val X = out Bool()
    //    val Y = out Bool()
    //    val T = out Bool()
    //    val Z = out Bool()

    val addDone = in Bool()
    val subDone = in Bool()
    val mulDone = in Bool()

    val fadd1_start = out Bool()
    val fsub1_start = out Bool()
    val fmul1_start = out Bool()

    val fadd2_start = out Bool()
    val fsub2_start = out Bool()
    val fmul2_start = out Bool()

    val p_X = in Bits (256 bits)
    val p_Y = in Bits (256 bits)
    val p_Z = in Bits (256 bits)
    val p_T = in Bits (256 bits)

    val q_YplusX = in Bits (256 bits)
    val q_YminusX = in Bits (256 bits)
    val q_Z = in Bits (256 bits)
    val q_T2D = in Bits (256 bits)

    val r_X = out Bits (256 bits)
    val r_Y = out Bits (256 bits)
    val r_Z = out Bits (256 bits)
    val r_T = out Bits (256 bits)


    val mul1_A = out Bits (256 bits)
    val mul1_B = out Bits (256 bits)
    val mul1_OUT = in Bits (256 bits)

    val add1_A = out Bits (256 bits)
    val add1_B = out Bits (256 bits)
    val add1_OUT = in Bits (256 bits)

    val sub1_A = out Bits (256 bits)
    val sub1_B = out Bits (256 bits)
    val sub1_OUT = in Bits (256 bits)

    val mul2_A = out Bits (256 bits)
    val mul2_B = out Bits (256 bits)
    val mul2_OUT = in Bits (256 bits)

    val add2_A = out Bits (256 bits)
    val add2_B = out Bits (256 bits)
    val add2_OUT = in Bits (256 bits)

    val sub2_A = out Bits (256 bits)
    val sub2_B = out Bits (256 bits)
    val sub2_OUT = in Bits (256 bits)


  }

  val clk = in Bool()
  val rst = in Bool()

  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {

    val r_X = Reg(Bits(256 bits))
    val r_Y = Reg(Bits(256 bits))
    val r_Z = Reg(Bits(256 bits))
    val r_T = Reg(Bits(256 bits))

    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntAddMul1 = new State
      val cntAddMul2 = new State
      val cntAddSub1 = new State
      val cntAddSub2 = new State

      io.fadd1_start := False
      io.fsub1_start := False
      io.fmul1_start := False
      io.fadd2_start := False
      io.fsub2_start := False
      io.fmul2_start := False

      io.add1_A := B(0, 256 bits)
      io.add1_B := B(0, 256 bits)
      io.sub1_A := B(0, 256 bits)
      io.sub1_B := B(0, 256 bits)
      io.mul1_A := B(0, 256 bits)
      io.mul1_B := B(0, 256 bits)

      io.add2_A := B(0, 256 bits)
      io.add2_B := B(0, 256 bits)
      io.sub2_A := B(0, 256 bits)
      io.sub2_B := B(0, 256 bits)
      io.mul2_A := B(0, 256 bits)
      io.mul2_B := B(0, 256 bits)

      io.r_X := B(0, 256 bits)
      io.r_Y := B(0, 256 bits)
      io.r_T := B(0, 256 bits)
      io.r_Z := B(0, 256 bits)
      io.done := False

      r_X := r_X
      r_Y := r_Y
      r_T := r_T
      r_Z := r_Z

      cntInit
        .whenIsActive {
          when(io.start === True) {
            goto(cntAddMul1)
          }
        }
      cntAddMul1
        .onEntry {
          io.fadd1_start := True
          io.fsub1_start := True
          io.fmul1_start := True
          io.fmul2_start := True
          io.add1_A := io.p_Y
          io.add1_B := io.p_X
          io.sub1_A := io.p_Y
          io.sub1_B := io.p_X
          io.mul1_A := io.p_T
          io.mul1_B := io.q_T2D
          io.mul2_A := io.p_Z
          io.mul2_B := io.q_Z
        }
        .whenIsActive {
          when(io.mulDone === True) {
            r_T := io.mul1_OUT
            goto(cntAddMul2)
          }
        }
      cntAddMul2
        .onEntry {
          io.fadd1_start := True
          io.fmul1_start := True
          io.fmul2_start := True
          io.add1_A := io.mul2_OUT
          io.add1_B := io.mul2_OUT
          io.mul1_A := io.add1_OUT
          io.mul1_B := io.q_YplusX
          io.mul2_A := io.sub1_OUT
          io.mul2_B := io.q_YminusX
        }
        .whenIsActive {
          when(io.mulDone === True) {
            goto(cntAddSub1)
          }
        }
      cntAddSub1
        .onEntry {
          io.fadd1_start := True
          io.fsub1_start := True
          io.fadd2_start := True
          io.fsub2_start := True
          io.add1_A := io.mul1_OUT
          io.add1_B := io.mul2_OUT
          io.sub1_A := io.mul1_OUT
          io.sub1_B := io.mul2_OUT
          io.add2_A := io.add1_OUT
          io.add2_B := r_T
          io.sub2_A := io.add1_OUT
          io.sub2_B := r_T
        }
        .whenIsActive {
          when(io.addDone === True) {
            io.r_X := io.sub1_OUT
            io.r_Y := io.add1_OUT
            io.r_Z := io.add2_OUT
            io.r_T := io.sub2_OUT
            io.done := True
            goto(cntInit)
          }
        }
    }

  }

}

case class mul512() extends Component {
  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()
    //    val X = out Bool()
    //    val Y = out Bool()
    //    val T = out Bool()
    //    val Z = out Bool()


    val mulDone = in Bool()


    val fmul1_start = out Bool()
    val fmul2_start = out Bool()

    val p_X = in Bits (256 bits)
    val p_Y = in Bits (256 bits)

    val size_A1_in = in Bits (4 bits)
    val size_A2_in = in Bits (4 bits)

    val size_A1_out = out Bits (4 bits)
    val size_A2_out = out Bits (4 bits)


    val r_X = out Bits (256 bits)
    val r_Y = out Bits (256 bits)


    val mul1_A = out Bits (256 bits)
    val mul1_B = out Bits (256 bits)
    val mul1_OUT = in Bits (512 bits)

    val mul2_A = out Bits (256 bits)
    val mul2_B = out Bits (256 bits)
    val mul2_OUT = in Bits (512 bits)

  }

  val clk = in Bool()
  val rst = in Bool()



  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {

    val r_X = Reg(Bits(256 bits))
    val r_Y = Reg(Bits(256 bits))

    val size1 = Reg(Bits(4 bits))
    val size2 = Reg(Bits(4 bits))


    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntMul1 = new State

      io.fmul1_start := False
      io.fmul2_start := False

      io.mul1_A := B(0, 256 bits)
      io.mul1_B := B(0, 256 bits)

      io.mul2_A := B(0, 256 bits)
      io.mul2_B := B(0, 256 bits)

      io.r_X := B(0, 256 bits)
      io.r_Y := B(0, 256 bits)

      io.done := False

      r_X := r_X
      r_Y := r_Y

      size1 := size1
      size2 := size2


      cntInit
        .whenIsActive {
          when(io.start === True) {
            goto(cntMul1)
          }
        }
      cntMul1
        .onEntry {
          io.fmul1_start := True
          io.mul1_A := io.p_X
          io.mul1_B := io.p_Y
          size1 := io.size_A1_in
          size2 := io.size_A2_in
        }
        .whenIsActive {
          when(io.mulDone === True) {
            io.r_X := io.mul1_OUT(511 downto 256)
            io.r_Y := io.mul1_OUT(255 downto 0)
            io.done := True
            goto(cntInit)
          }
        }

    }

  }

  io.size_A1_out := coreArea.size1
  io.size_A2_out := coreArea.size2

}

case class reduceL() extends Component {
  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()
    //    val X = out Bool()
    //    val Y = out Bool()
    //    val T = out Bool()
    //    val Z = out Bool()


    val add1Done = in Bool()
    val sub1Done = in Bool()
    val mul1Done = in Bool()

    val add2Done = in Bool()
    val sub2Done = in Bool()
    val mul2Done = in Bool()

    val fadd1_start = out Bool()
    val fsub1_start = out Bool()
    val fmul1_start = out Bool()

    val fadd2_start = out Bool()
    val fsub2_start = out Bool()
    val fmul2_start = out Bool()

    val p_X = in Bits (256 bits)
    val p_Y = in Bits (256 bits)

    val r_X = out Bits (256 bits)
    val r_Y = out Bits (256 bits)
    val r_Z = out Bits (256 bits)
    val r_T = out Bits (256 bits)


    val mul1_A = out Bits (256 bits)
    val mul1_B = out Bits (256 bits)
    val mul1_sizeA = out Bits(4 bits)
    val mul1_OUT = in Bits (256 bits)
    val mul1_512 = in Bits (512 bits)

    val add1_A = out Bits (256 bits)
    val add1_B = out Bits (256 bits)
    val add1_carryIn = out Bits(1 bit)
    val add1_OUT = in Bits (256 bits)
    val add1_carryOUT = in Bits(1 bit)

    val sub1_A = out Bits (256 bits)
    val sub1_B = out Bits (256 bits)
    val sub1_carryIn = out Bits(1 bit)
    val sub1_OUT = in Bits (256 bits)
    val sub1_carryOUT = in Bits(1 bit)

    val mul2_A = out Bits (256 bits)
    val mul2_B = out Bits (256 bits)
    val mul2_sizeA = out Bits(4 bits)
    val mul2_OUT = in Bits (256 bits)
    val mul2_512 = in Bits (512 bits)

    val add2_A = out Bits (256 bits)
    val add2_B = out Bits (256 bits)
    val add2_OUT = in Bits (256 bits)

    val sub2_A = out Bits (256 bits)
    val sub2_B = out Bits (256 bits)
    val sub2_OUT = in Bits (256 bits)


  }

  val clk = in Bool()
  val rst = in Bool()

  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {
    val r_X = Reg(Bits(256 bits))
    val r_Y = Reg(Bits(256 bits))
    val r_Z = Reg(Bits(256 bits))
    val r_T = Reg(Bits(256 bits))

    val size1 = Reg(Bits(4 bits))
    val size2 = Reg(Bits(4 bits))

    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntAddMul1 = new State
      val cntAdd2 = new State
      val cntSub1 = new State
      val cntSub2 = new State
      val cntMul2 = new State
      val cntAdd3 = new State
      val cntAdd4 = new State
      val cntMul3 = new State
      val cntSub3 = new State
      val cntSub4 = new State


      io.fadd1_start := False
      io.fsub1_start := False
      io.fmul1_start := False
      io.fadd2_start := False
      io.fsub2_start := False
      io.fmul2_start := False

      io.add1_A := B(0, 256 bits)
      io.add1_B := B(0, 256 bits)
      io.add1_carryIn := B(0, 1 bit)
      io.sub1_A := B(0, 256 bits)
      io.sub1_B := B(0, 256 bits)
      io.sub1_carryIn := B(0, 1 bit)
      io.mul1_A := B(0, 256 bits)
      io.mul1_B := B(0, 256 bits)
      io.mul1_sizeA := B(15, 4 bits)

      io.add2_A := B(0, 256 bits)
      io.add2_B := B(0, 256 bits)
      io.sub2_A := B(0, 256 bits)
      io.sub2_B := B(0, 256 bits)
      io.mul2_A := B(0, 256 bits)
      io.mul2_B := B(0, 256 bits)
      io.mul2_sizeA := B(15, 4 bits)

      io.r_X := B(0, 256 bits)
      io.r_Y := B(0, 256 bits)
      io.r_T := B(0, 256 bits)
      io.r_Z := B(0, 256 bits)
      io.done := False

      r_X := r_X
      r_Y := r_Y
      r_T := r_T
      r_Z := r_Z

      cntInit
        .whenIsActive {
          when(io.start === True) {
            goto(cntAddMul1)
          }
        }
      cntAddMul1
        .onEntry {
          io.fadd1_start := True
          io.fmul1_start := True
          io.fmul2_start := True
          io.add1_A := B"256'h9bdf3bd45ef39acb024c634b9eba7da000000000000000000000000000000000"
          io.add1_B := io.p_Y(251 downto 0).resize(256 bits)
          io.mul1_A := B"256'h14def9dea2f79cd65812631a5cf5d3ed"
          io.mul1_B := Cat(B(0, 126 bits), io.p_X(125 downto 0), io.p_Y(255 downto 252))
          io.mul1_sizeA := B(7, 4 bits)
          io.mul2_A := B"256'h14def9dea2f79cd65812631a5cf5d3ed"
          io.mul2_B := Cat(B(0, 126 bits), io.p_X(255 downto 126))
          io.mul2_sizeA := B(7, 4 bits)

          r_X := io.p_X
          r_Y := io.p_Y
        }
        .whenIsActive {
          io.mul1_sizeA := B(7, 4 bits)
          io.mul2_sizeA := B(7, 4 bits)
          when(io.mul1Done === True) {
            goto(cntAdd2)
          }
        }
      cntAdd2
        .onEntry {
          io.fadd2_start := True
          io.add2_A := io.mul1_512(257 downto 130).resize(256 bits)
          io.add2_B := io.mul2_512(255 downto 0)
        }
        .whenIsActive {
          when(io.add2Done === True) {
            goto(cntSub1)
          }
        }
      cntSub1
        .onEntry {
          io.fsub1_start := True
          io.sub1_A := io.add1_OUT
          io.sub1_B := Cat(io.add2_OUT(125 downto 0), io.mul1_512(129 downto 0))
        }
        .whenIsActive {
          when(io.sub1Done === True) {
            r_Y := io.sub1_OUT
            goto(cntSub2)
          }
        }
      cntSub2
        .onEntry {
          io.fsub1_start := True
          io.sub1_A := B"256'h200000000000000000000000000000002"
          io.sub1_B := io.add2_OUT(255 downto 126).resize(256 bits)
          io.sub1_carryIn := io.sub1_carryOUT
        }
        .whenIsActive {
          when(io.sub1Done === True) {
            r_X := io.sub1_OUT
            goto(cntMul2)
          }
        }
      cntMul2
        .onEntry{
          io.fmul1_start := True
          io.fmul2_start := True
          io.mul1_A := r_Y(192 downto 0).resize(256 bits)
          io.mul1_B := B"256'h3fffffffffffffffffffffffffffffffac84188574218ca69fb673968c28b04c"
          io.mul1_sizeA := B(12, 4 bits)
          io.mul2_A := Cat(io.sub1_OUT(129 downto 0), r_Y(255 downto 193)).resize(256 bits)
          io.mul2_B := B"256'h3fffffffffffffffffffffffffffffffac84188574218ca69fb673968c28b04c"
          io.mul2_sizeA := B(12, 4 bits)
        }
        .whenIsActive {
          io.mul1_sizeA := B(12, 4 bits)
          io.mul2_sizeA := B(12, 4 bits)
          when(io.mul1Done === True) {
            goto(cntAdd3)
          }
        }
      cntAdd3
        .onEntry{
          io.fadd1_start := True
          io.add1_A := io.mul1_512(448 downto 193)
          io.add1_B := io.mul2_512(255 downto 0)
        }
        .whenIsActive {
          when(io.add1Done === True) {
            goto(cntAdd4)
          }
        }
      cntAdd4
        .onEntry {
          io.fadd1_start := True
          io.add1_A := B(0, 256 bits)
          io.add1_B := io.mul2_512(448 downto 256).resize(256 bits)
          io.add1_carryIn := io.add1_carryOUT
        }
        .whenIsActive {
          when(io.add1Done === True) {
            goto(cntMul3)
          }
        }
      cntMul3
        .onEntry {
          io.fmul1_start := True
          io.mul1_A := io.add1_OUT(255 downto 57).resize(256 bits)
          io.mul1_B := B"256'h1000000000000000000000000000000014def9dea2f79cd65812631a5cf5d3ed"
          io.mul1_sizeA := B(8, 4 bits)
        }
        .whenIsActive {
          io.mul1_sizeA := B(8, 4 bits)
          when(io.mul1Done === True) {
            goto(cntSub3)
          }
        }
      cntSub3
        .onEntry {
          io.fsub1_start := True
          io.sub1_A := r_Y
          io.sub1_B := io.mul1_512(255 downto 0)
        }
        .whenIsActive {
          when(io.sub1Done === True) {
            r_Y := io.sub1_OUT
            goto(cntSub4)
          }
        }
      cntSub4
        .onEntry {
          io.fsub1_start := True
          io.sub1_A := io.sub1_OUT
          io.sub1_B := B"256'h1000000000000000000000000000000014def9dea2f79cd65812631a5cf5d3ed"
        }
        .whenIsActive {
          when(io.sub1Done === True) {
            io.r_X := (io.sub1_OUT(255 downto 255) === 1)? r_Y | io.sub1_OUT
            io.done := True
            goto(cntInit)
          }
        }
    }
  }
}


case class mulmod25519() extends Component{
  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()

    val mulDone = in Bool()


    val fmul1_start = out Bool()
    val fmul2_start = out Bool()

    val p_X = in Bits (256 bits)
    val p_Y = in Bits (256 bits)
    val p_Z = in Bits (256 bits)
    val p_T = in Bits (256 bits)

    val r_X = out Bits (256 bits)
    val r_Y = out Bits (256 bits)

    val mul1_A = out Bits (256 bits)
    val mul1_B = out Bits (256 bits)
    val mul1_OUT = in Bits (256 bits)

    val mul2_A = out Bits (256 bits)
    val mul2_B = out Bits (256 bits)
    val mul2_OUT = in Bits (256 bits)


  }

  val clk = in Bool()
  val rst = in Bool()

  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {

    val r_X = Reg(Bits(256 bits))
    val r_Y = Reg(Bits(256 bits))

    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntMul1 = new State

      io.fmul1_start := False
      io.fmul2_start := False

      io.mul1_A := B(0, 256 bits)
      io.mul1_B := B(0, 256 bits)

      io.mul2_A := B(0, 256 bits)
      io.mul2_B := B(0, 256 bits)

      io.r_X := B(0, 256 bits)
      io.r_Y := B(0, 256 bits)
      io.done := False

      cntInit
        .whenIsActive {
          when(io.start === True) {
            goto(cntMul1)
          }
        }
      cntMul1
        .onEntry {
          io.fmul1_start := True
          io.fmul2_start := True
          io.mul1_A := io.p_X
          io.mul1_B := io.p_Y
          io.mul2_A := io.p_Z
          io.mul2_B := io.p_T
        }
        .whenIsActive {
          when(io.mulDone === True) {
            io.r_X := io.mul1_OUT
            io.r_Y := io.mul2_OUT
            io.done := True
            goto(cntInit)
          }
        }
    }

  }
}

case class addmod25519() extends Component{
  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()

    val addDone = in Bool()


    val fadd1_start = out Bool()
    val fadd2_start = out Bool()

    val p_X = in Bits (256 bits)
    val p_Y = in Bits (256 bits)
    val p_Z = in Bits (256 bits)
    val p_T = in Bits (256 bits)

    val r_X = out Bits (256 bits)
    val r_Y = out Bits (256 bits)

    val add1_A = out Bits (256 bits)
    val add1_B = out Bits (256 bits)
    val add1_OUT = in Bits (256 bits)

    val add2_A = out Bits (256 bits)
    val add2_B = out Bits (256 bits)
    val add2_OUT = in Bits (256 bits)


  }

  val clk = in Bool()
  val rst = in Bool()

  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {

    val r_X = Reg(Bits(256 bits))
    val r_Y = Reg(Bits(256 bits))

    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntAdd1 = new State

      io.fadd1_start := False
      io.fadd2_start := False

      io.add1_A := B(0, 256 bits)
      io.add1_B := B(0, 256 bits)

      io.add2_A := B(0, 256 bits)
      io.add2_B := B(0, 256 bits)

      io.r_X := B(0, 256 bits)
      io.r_Y := B(0, 256 bits)
      io.done := False

      cntInit
        .whenIsActive {
          when(io.start === True) {
            goto(cntAdd1)
          }
        }
      cntAdd1
        .onEntry {
          io.fadd1_start := True
          io.fadd2_start := True
          io.add1_A := io.p_X
          io.add1_B := io.p_Y
          io.add2_A := io.p_Z
          io.add2_B := io.p_T
        }
        .whenIsActive {
          when(io.addDone === True) {
            io.r_X := io.add1_OUT
            io.r_Y := io.add2_OUT
            io.done := True
            goto(cntInit)
          }
        }
    }

  }
}

case class submod25519() extends Component{
  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()

    val subDone = in Bool()


    val fsub1_start = out Bool()
    val fsub2_start = out Bool()

    val p_X = in Bits (256 bits)
    val p_Y = in Bits (256 bits)
    val p_Z = in Bits (256 bits)
    val p_T = in Bits (256 bits)

    val r_X = out Bits (256 bits)
    val r_Y = out Bits (256 bits)

    val sub1_A = out Bits (256 bits)
    val sub1_B = out Bits (256 bits)
    val sub1_OUT = in Bits (256 bits)

    val sub2_A = out Bits (256 bits)
    val sub2_B = out Bits (256 bits)
    val sub2_OUT = in Bits (256 bits)


  }

  val clk = in Bool()
  val rst = in Bool()

  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {

    val r_X = Reg(Bits(256 bits))
    val r_Y = Reg(Bits(256 bits))

    val cnt_fsm = new StateMachine {
      val cntInit = new State with EntryPoint
      val cntSub1 = new State

      io.fsub1_start := False
      io.fsub2_start := False

      io.sub1_A := B(0, 256 bits)
      io.sub1_B := B(0, 256 bits)

      io.sub2_A := B(0, 256 bits)
      io.sub2_B := B(0, 256 bits)

      io.r_X := B(0, 256 bits)
      io.r_Y := B(0, 256 bits)
      io.done := False

      cntInit
        .whenIsActive {
          when(io.start === True) {
            goto(cntSub1)
          }
        }
      cntSub1
        .onEntry {
          io.fsub1_start := True
          io.fsub2_start := True
          io.sub1_A := io.p_X
          io.sub1_B := io.p_Y
          io.sub2_A := io.p_Z
          io.sub2_B := io.p_T
        }
        .whenIsActive {
          when(io.subDone === True) {
            io.r_X := io.sub1_OUT
            io.r_Y := io.sub2_OUT
            io.done := True
            goto(cntInit)
          }
        }
    }

  }
}