//package vexriscv.ip.frost
//
//import scala.util.Random
//
//import spinal.core._
//import spinal.lib._
//import spinal.lib.bus.amba4.axi._
//import spinal.lib.bus.amba4.axilite._
//import spinal.lib.fsm._
//
//case class fsum() extends Component{
//
//  val io = new Bundle {
//    val A = in Vec(Bits(32 bits), 8)
//    val B = in Vec(Bits(32 bits), 8)
//    val OUT = out Vec(Bits(32 bits), 8)
//  }
//
//  val Med = Vec.fill(8)(Bits(33 bits))
//  Med(0) := (io.A(0).asUInt + io.B(0).asUInt).asBits
//  Med(1) := (io.A(1).asUInt + io.B(1).asUInt + Med(0)(32 downto 32).asUInt).asBits.resize(33 bits)
//  Med(2) := (io.A(2).asUInt + io.B(2).asUInt + Med(1)(32 downto 32).asUInt).asBits.resize(33 bits)
//  Med(3) := (io.A(3).asUInt + io.B(3).asUInt + Med(2)(32 downto 32).asUInt).asBits.resize(33 bits)
//  Med(4) := (io.A(4).asUInt + io.B(4).asUInt + Med(3)(32 downto 32).asUInt).asBits.resize(33 bits)
//  Med(5) := (io.A(5).asUInt + io.B(5).asUInt + Med(4)(32 downto 32).asUInt).asBits.resize(33 bits)
//  Med(6) := (io.A(6).asUInt + io.B(6).asUInt + Med(5)(32 downto 32).asUInt).asBits.resize(33 bits)
//  Med(7) := (io.A(7).asUInt + io.B(7).asUInt + Med(6)(32 downto 32).asUInt).asBits.resize(33 bits)
//
//  io.OUT(0) := Med(0)(31 downto 0)
//  io.OUT(1) := Med(1)(31 downto 0)
//  io.OUT(2) := Med(2)(31 downto 0)
//  io.OUT(3) := Med(3)(31 downto 0)
//  io.OUT(4) := Med(4)(31 downto 0)
//  io.OUT(5) := Med(5)(31 downto 0)
//  io.OUT(6) := Med(6)(31 downto 0)
//  io.OUT(7) := Med(7)(31 downto 0)
//
//}
//
//case class fsum_mod() extends Component{
//
//  val io = new Bundle {
//    val A = in Vec(Bits(32 bits), 8)
//    val B = in Vec(Bits(32 bits), 8)
//    val OUT = out Vec(Bits(32 bits), 8)
//  }
//
//  println("here F")
//  val Med = Vec.fill(8)(Bits(33 bits))
//  Med(0) := (io.A(0).asUInt.resize(33 bits) + io.B(0).asUInt.resize(33 bits)).asBits
//  Med(1) := (io.A(1).asUInt.resize(33 bits) + io.B(1).asUInt.resize(33 bits) + Med(0)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med(2) := (io.A(2).asUInt.resize(33 bits) + io.B(2).asUInt.resize(33 bits) + Med(1)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med(3) := (io.A(3).asUInt.resize(33 bits) + io.B(3).asUInt.resize(33 bits) + Med(2)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med(4) := (io.A(4).asUInt.resize(33 bits) + io.B(4).asUInt.resize(33 bits) + Med(3)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med(5) := (io.A(5).asUInt.resize(33 bits) + io.B(5).asUInt.resize(33 bits) + Med(4)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med(6) := (io.A(6).asUInt.resize(33 bits) + io.B(6).asUInt.resize(33 bits) + Med(5)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med(7) := (io.A(7).asUInt.resize(33 bits) + io.B(7).asUInt.resize(33 bits) + Med(6)(32 downto 32).asUInt.resize(33 bits)).asBits
//
//  println("here G")
//
//  val Med2 = Vec.fill(8)(Bits(33 bits))
//  Med2(0) := (Med(0)(31 downto 0).asUInt.resize(33 bits) + U(19, 32 bits).resize(33 bits)).asBits
//  Med2(1) := (Med(1)(31 downto 0).asUInt.resize(33 bits) + Med2(0)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med2(2) := (Med(2)(31 downto 0).asUInt.resize(33 bits) + Med2(1)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med2(3) := (Med(3)(31 downto 0).asUInt.resize(33 bits) + Med2(2)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med2(4) := (Med(4)(31 downto 0).asUInt.resize(33 bits) + Med2(3)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med2(5) := (Med(5)(31 downto 0).asUInt.resize(33 bits) + Med2(4)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med2(6) := (Med(6)(31 downto 0).asUInt.resize(33 bits) + Med2(5)(32 downto 32).asUInt.resize(33 bits)).asBits
//  Med2(7) := (Med(7)(31 downto 0).asUInt.resize(33 bits) + Med2(6)(32 downto 32).asUInt.resize(33 bits)).asBits
//
//  println("here H")
//
//  io.OUT(0) := Med2(7)(31 downto 31).asBool ? (Med2(0)(31 downto 0)) | (Med(0)(31 downto 0))
//  io.OUT(1) := Med2(7)(31 downto 31).asBool ? (Med2(1)(31 downto 0)) | (Med(1)(31 downto 0))
//  io.OUT(2) := Med2(7)(31 downto 31).asBool ? (Med2(2)(31 downto 0)) | (Med(2)(31 downto 0))
//  io.OUT(3) := Med2(7)(31 downto 31).asBool ? (Med2(3)(31 downto 0)) | (Med(3)(31 downto 0))
//  io.OUT(4) := Med2(7)(31 downto 31).asBool ? (Med2(4)(31 downto 0)) | (Med(4)(31 downto 0))
//  io.OUT(5) := Med2(7)(31 downto 31).asBool ? (Med2(5)(31 downto 0)) | (Med(5)(31 downto 0))
//  io.OUT(6) := Med2(7)(31 downto 31).asBool ? (Med2(6)(31 downto 0)) | (Med(6)(31 downto 0))
//  io.OUT(7) := Med2(7)(31 downto 31).asBool ? (Med2(7)(30 downto 0)).resize(32 bits) | (Med(7)(30 downto 0)).resize(32 bits)
//
//  println("here I")
//}
//
//case class fsum_mod_wrapper() extends Component{
//
//  val io = new Bundle {
//    val start = in Bool()
//    val A = in Bits (256 bits)
//    val B = in Bits (256 bits)
//    val OUT = out Bits(256 bits)
//    val done = out Bool()
//  }
//
//  val clk = in Bool()
//  val rst = in Bool()
//
//  val A_reg = Bits(256 bits)
//  val B_reg = Bits(256 bits)
//
//  val add_comb = fsum_mod()
//
//  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
//  val coreArea = new ClockingArea(clkDomain) {
//    A_reg := RegNextWhen(io.A, io.start, B(0, 256 bits))
//    B_reg := RegNextWhen(io.B, io.start, B(0, 256 bits))
//    for (i <- 0 until 8) {
//      add_comb.io.A(i) := A_reg(32 * (i + 1) - 1 downto 32 * i)
//      add_comb.io.B(i) := B_reg(32 * (i + 1) - 1 downto 32 * i)
//      io.OUT(32 * (i + 1) - 1 downto 32 * i) := RegNextWhen(add_comb.io.OUT(i), RegNext(io.start), B(0, 32 bits))
//    }
//    io.done := RegNext(RegNext(io.start))
//  }
//
//}
//
//case class fsub_mod()extends Component{
//
//  val io = new Bundle {
//    val A = in Vec(Bits(32 bits), 8)
//    val B = in Vec(Bits(32 bits), 8)
//    val OUT = out Vec(Bits(32 bits), 8)
//  }
//
//  val Med = Vec.fill(8)(Bits(33 bits))
//  Med(0) := (io.A(0).resize(33 bits).asSInt - io.B(0).resize(33 bits).asSInt).asBits
//  Med(1) := (io.A(1).resize(33 bits).asSInt - io.B(1).resize(33 bits).asSInt - Med(0)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med(2) := (io.A(2).resize(33 bits).asSInt - io.B(2).resize(33 bits).asSInt - Med(1)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med(3) := (io.A(3).resize(33 bits).asSInt - io.B(3).resize(33 bits).asSInt - Med(2)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med(4) := (io.A(4).resize(33 bits).asSInt - io.B(4).resize(33 bits).asSInt - Med(3)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med(5) := (io.A(5).resize(33 bits).asSInt - io.B(5).resize(33 bits).asSInt - Med(4)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med(6) := (io.A(6).resize(33 bits).asSInt - io.B(6).resize(33 bits).asSInt - Med(5)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med(7) := (io.A(7).resize(33 bits).asSInt - io.B(7).resize(33 bits).asSInt - Med(6)(32 downto 32).resize(33 bits).asSInt).asBits
//
//  val Med2 = Vec.fill(8)(Bits(33 bits))
//  Med2(0) := (Med(0)(31 downto 0).resize(33 bits).asSInt + S(-19, 32 bits).resize(33 bits)).asBits
//  Med2(1) := (Med(1)(31 downto 0).resize(33 bits).asSInt - Med2(0)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med2(2) := (Med(2)(31 downto 0).resize(33 bits).asSInt - Med2(1)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med2(3) := (Med(3)(31 downto 0).resize(33 bits).asSInt - Med2(2)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med2(4) := (Med(4)(31 downto 0).resize(33 bits).asSInt - Med2(3)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med2(5) := (Med(5)(31 downto 0).resize(33 bits).asSInt - Med2(4)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med2(6) := (Med(6)(31 downto 0).resize(33 bits).asSInt - Med2(5)(32 downto 32).resize(33 bits).asSInt).asBits
//  Med2(7) := (Med(7)(31 downto 0).resize(33 bits).asSInt - Med2(6)(32 downto 32).resize(33 bits).asSInt).asBits
//
//  io.OUT(0) := Med(7)(31 downto 31).asBool ? (Med2(0)(31 downto 0)) | (Med(0)(31 downto 0))
//  io.OUT(1) := Med(7)(31 downto 31).asBool ? (Med2(1)(31 downto 0)) | (Med(1)(31 downto 0))
//  io.OUT(2) := Med(7)(31 downto 31).asBool ? (Med2(2)(31 downto 0)) | (Med(2)(31 downto 0))
//  io.OUT(3) := Med(7)(31 downto 31).asBool ? (Med2(3)(31 downto 0)) | (Med(3)(31 downto 0))
//  io.OUT(4) := Med(7)(31 downto 31).asBool ? (Med2(4)(31 downto 0)) | (Med(4)(31 downto 0))
//  io.OUT(5) := Med(7)(31 downto 31).asBool ? (Med2(5)(31 downto 0)) | (Med(5)(31 downto 0))
//  io.OUT(6) := Med(7)(31 downto 31).asBool ? (Med2(6)(31 downto 0)) | (Med(6)(31 downto 0))
//  io.OUT(7) := Med(7)(31 downto 31).asBool ? (Med2(7)(30 downto 0)).resize(32 bits) | (Med(7)(30 downto 0)).resize(32 bits)
//
//}
//
//case class fsub_mod_wrapper() extends Component{
//
//  val io = new Bundle {
//    val start = in Bool()
//    val A = in Bits (256 bits)
//    val B = in Bits (256 bits)
//    val OUT = out Bits(256 bits)
//    val done = out Bool()
//  }
//
//  val clk = in Bool()
//  val rst = in Bool()
//
//  val A_reg = Bits(256 bits)
//  val B_reg = Bits(256 bits)
//
//
//  val sub_comb = fsub_mod()
//
//  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
//  val coreArea = new ClockingArea(clkDomain) {
//    A_reg := RegNextWhen(io.A, io.start, B(0, 256 bits))
//    B_reg := RegNextWhen(io.B, io.start, B(0, 256 bits))
//    for (i <- 0 until 8) {
//      sub_comb.io.A(i) := A_reg(32 * (i + 1) - 1 downto 32 * i)
//      sub_comb.io.B(i) := B_reg(32 * (i + 1) - 1 downto 32 * i)
//      io.OUT(32 * (i + 1) - 1 downto 32 * i) := RegNextWhen(sub_comb.io.OUT(i), RegNext(io.start), B(0, 32 bits))
//    }
//    io.done := RegNext(RegNext(io.start))
//  }
//
//}
//
//case class fmul_limb() extends Component {
//   val io = new Bundle{
//     val A = in Bits(22 bits)
//     val B = in Bits(16 bits)
//     val OUT = out Bits(38 bits)
//   }
//
//  io.OUT := (io.A.asUInt * io.B.asUInt).asBits
//}
//
//case class fadd_limb() extends Component {
//  val io = new Bundle{
//    val A = in Bits(38 bits)
//    val B = in Bits(22 bits)
//    val OUT = out Bits(38 bits)
//  }
//
//  io.OUT := (io.A.asUInt + io.B.asUInt)(37 downto 0).asBits
//}
//
//case class fmac_limb() extends Component {
//  val io = new Bundle{
//    val A = in Bits (22 bits)
//    val B = in Bits (16 bits)
//    val Carry = in Bits (22 bits)
//    val OUT = out Bits (38 bits)
//    val mulOut = out Bits (38 bits)
//  }
//
//  val mul0 = new fmul_limb()
//  val add0 = new fadd_limb()
//
//  mul0.io.A := io.A
//  mul0.io.B := io.B
//  io.mulOut := mul0.io.OUT
//
//  add0.io.A := mul0.io.OUT
//  add0.io.B := io.Carry
//  io.OUT := add0.io.OUT
//}
//
//case class facc_limbs() extends Component {
//  val io = new Bundle{
//    val A = in Vec(Bits(20 bits), 16)
//    val Carry = in Bits(20 bits)
//    val Out = out Bits(20 bits)
//  }
//
//  io.Out := (io.A(0).asUInt + io.A(1).asUInt + io.A(2).asUInt + io.A(3).asUInt +io.A(4).asUInt + io.A(5).asUInt + io.A(6).asUInt + io.A(7).asUInt +
//    io.A(8).asUInt + io.A(9).asUInt + io.A(10).asUInt + io.A(11).asUInt +io.A(12).asUInt + io.A(13).asUInt + io.A(14).asUInt + io.A(15).asUInt + io.Carry.asUInt).asBits.resize(20 bits)
//
//}
//
//case class fmul_mod() extends Component {
//
//  val io = new Bundle {
//    val start = in Bool()
//    //    val done = out Bool()
//    val A = in Vec(Bits(16 bits), 16)
//    val B = in Vec(Bits(16 bits), 16)
//    val cnt_out = out UInt(5 bits)
////    val add_out = in Vec(Bits(32 bits), 8)
//    val add_in_A = out Vec(Bits(32 bits), 8)
//    val add_in_B = out Bits(32 bits)
////    val OUT = out Vec(Bits(32 bits), 8)
//  }
//
//  val clk = in Bool()
//  val rst = in Bool()
//
//  val macs = Vector.fill(16)(fmac_limb())
//  val acc = facc_limbs()
//
//  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
//  val coreArea = new ClockingArea(clkDomain) {
//
//    val A_reg = Vec(Bits(22 bits), 32)
//    val B_reg = Vec(Bits(16 bits), 16)
//
//    val mac_out = Vec(Bits(38 bits), 16)
//    val carry_reg = Bits(4 bits)
//
//    val mask = Bits(16 bits)
//
//    val cnt = Reg(UInt(5 bits))
//
//    val cnt_fsm = new StateMachine {
//      val cnt_init = new State with EntryPoint
//      val cnt_change = new State
//
//      cnt_init
//        .onEntry {
//          cnt := U(0, 5 bits)
//        }
//        .whenIsActive {
//          when(io.start === True) {
//            goto(cnt_change)
//          }
//        }
//      cnt_change
//        .onEntry {
//          cnt := cnt + 1
//        }
//        .whenIsActive {
//          cnt := cnt + 1
//          when(cnt === 20) {
//            //            io.done := True
//            goto(cnt_init)
//          }
//        }
//    }
//
//    B_reg := (cnt === 0)? RegNext(io.B) | RegNext(B_reg)
//
//    //    for (i <- 0 until 15) {
//    //      A_reg(i) := (cnt === 0)? Cat(io.A(15-i), macs(15-i).io.OUT(15 downto 0)) | Cat(B(0, 16 bits), A_reg(i-1))
//    //    }
//
//    A_reg(0) := (cnt === 0)? RegNext(io.A(15)).resize(22 bits) | ((cnt === 18)? RegNext(acc.io.Out(14 downto 0).resize(22 bits)) | RegNext(acc.io.Out.resize(22 bits)))
//    for (i <- 1 until 16) {
//      A_reg(i) := (cnt === 0)? RegNext(io.A(15-i)).resize(22 bits) | RegNext(A_reg(i-1))
//    }
//    for (i <- 16 until 32) {
//      A_reg(i) := (cnt === 0)? RegNext(macs(31-i).io.OUT(21 downto 0).asBits) | RegNext(A_reg(i - 1))
//    }
//
//    for (id <- 0 until 16) {
////      println(s"id: $id 15+id: ${15+id} ")
//      macs(id).io.B := (cnt === 0 || cnt === 17) ? B(19, 16 bits) | ((cnt >= 18)? B(0, 16 bits) | B_reg(id))
//      macs(id).io.A := (cnt === 0) ? Cat(io.A(id), B(0, 1 bit)).resize(22 bits) | ((cnt === 17) ? mac_out(id)(36 downto 15) | ((cnt >= 18)? B(0, 22 bits) | A_reg(16+id).resize(22 bits)))
//      //      mac.io.A := io.A(id).resized
//      macs(id).io.Carry := (cnt <= 1) ? B(0, 22 bits) | ((cnt === 17) ? (B(19, 5 bits) & ((mask(id), mask(id), mask(id), mask(id), mask(id)).asBits)).resize(22 bits) | mac_out(id)(37 downto 16))
//      mac_out(id) := RegNext(macs(id).io.OUT)
//      acc.io.A(id) := (cnt === 17) ? mac_out(id)(14 downto 0).resize(20 bits) | mac_out(id)(15 downto 0).resize(20 bits)
////      acc.io.A(id) := mac_out(id)(15 downto 0).resize(20 bits)i
//    }
//
//    carry_reg := RegNext(acc.io.Out(19 downto 16))
//    acc.io.Carry := (cnt <= 2 || cnt === 18)? B(0, 20 bits) | carry_reg.resize(20 bits)
//
//    mask := (cnt === 17)? ((B(1, 16 bits) << acc.io.Out(18 downto 15).asUInt).asUInt - 1).asBits.resize(16 bits) | B(0, 16 bits)
////    mask := (cnt === 17)? ((B(1, 16 bits) << acc.io.Out(19 downto 16).asUInt).asUInt - 1).asBits.resize(16 bits) | B(0, 16 bits)
//
//    println("here a")
//
//    for (id <- 0 until 7) {
//      io.add_in_A(id) := (cnt === 20)? Cat(A_reg(16 - 2*id)(15 downto 0), A_reg(17 - 2*id)(15 downto 0)) | B(0, 32 bits)
//    }
//
//    io.add_in_A(7) := (cnt === 20)? Cat(B(0, 1 bits), A_reg(2)(14 downto 0), A_reg(3)(15 downto 0)) | B(0, 32 bits)
//
//    println("here B")
//    io.add_in_B := (cnt === 20)? Cat(A_reg(0)(15 downto 0), A_reg(1)(15 downto 0)) | B(0, 32 bits)
//    println("here C")
//
//    io.cnt_out := cnt
//
////    for (id <- 0 until 8) {
////      io.OUT(id) := (cnt === 20) ? io.add_out(id) | B(0, 32 bits)
////    }
//
//
//    //    macs.zipWithIndex.foreach{ case(mac, id) =>
//    //      mac.io.B := (cnt === 0)? B(19, 16 bits) | B_reg(id)
//    //      mac.io.A := (cnt === 0)? io.A(id).resized | A_reg(15 + id).resized
//    ////      mac.io.A := io.A(id).resized
//    //      mac.io.Carry := (cnt <= 1)? B(0, 21 bits) | RegNext(mac_out(id)(36 downto 16))
//    //      mac_out(id) := RegNext(mac.io.OUT)
//    //      acc.io.A(id) := mac_out(id)(15 downto 0)
//    //    }
//
//
//    //    acc.io.A := mac_out
//
//
//    //    io.OUT(cnt) := acc.io.Out(15 downto 0)
//
//  }
//
//}
//
//case class fmul_wrapper() extends Component{
//  val io = new Bundle {
//    val start = in Bool()
//    val A = in Bits(256 bits)
//    val B = in Bits(256 bits)
//    val mode_mod19 = in Bits (1 bits)
//    val size_A = in Bits(4 bits)
//    val OUT = out Bits(256 bits)
//    val OUT512 = out Bits(512 bits)
//    val done = out Bool()
//  }
//
//  val clk = in Bool()
//  val rst = in Bool()
//
//  println("here D")
////  val fmul_inst = fmul_mod_combined_1()
////  val fmul_inst = fmul_mod_combined_new_timing()
//  val fmul_inst = fmul_mod_combined_new_attempt()
//  val fsum_inst = fsum_mod()
//
//  println("here E")
//
//  fmul_inst.io.start := io.start
//  fmul_inst.clk := clk
//  fmul_inst.rst := rst
//
//  fmul_inst.io.mode_mod19 := io.mode_mod19
//  fmul_inst.io.size_A := io.size_A
//
//  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
//  val coreArea = new ClockingArea(clkDomain) {
//    for (i <- 0 until 16) {
//      fmul_inst.io.A(i) := io.A(16 * (i + 1) - 1 downto 16 * i)
//      fmul_inst.io.B(i) := io.B(16 * (i + 1) - 1 downto 16 * i)
//    }
//
//    for (i <- 0 until 8) {
//      fsum_inst.io.A(i) := fmul_inst.io.add_in_A(i)
//      //    fmul_inst.io.add_out(i) := fsum_inst.io.OUT(i)
//      io.OUT(32 * (i + 1) - 1 downto 32 * i) := (io.mode_mod19 === B(1, 1 bits))? RegNextWhen(fsum_inst.io.OUT(i), fmul_inst.io.cnt_out === U(20, 5 bits), B(0, 32 bits)) | B(0, 32 bits)
//    }
//    for (i <- 1 until 8) {
//      fsum_inst.io.B(i) := B(0, 32 bits)
//    }
//    fsum_inst.io.B(0) := fmul_inst.io.add_in_B
//
//
//    ////////////////////////////////////////
//    for (i <- 0 until 32) {
//      io.OUT512(16 * (i+1) - 1 downto 16 * i) := (io.mode_mod19 === B(0, 1 bits))? RegNextWhen(fmul_inst.io.A_reg_modL(i), fmul_inst.io.cnt_out === (io.size_A.asUInt.resize(5 bits) + U(2, 5 bits)), B(0, 16 bits)) | B(0, 16 bits)
//    }
//    io.done := (io.mode_mod19 === B(1, 1 bits))? RegNext(fmul_inst.io.cnt_out === U(20, 5 bits)) | RegNext(fmul_inst.io.cnt_out === U(17, 5 bits))
//
//
//  }
//
//}
//
//
//case class fsum_16() extends Component{
//
//  val io = new Bundle {
//    val A = in Vec(Bits(16 bits), 16)
//    val B = in Vec(Bits(16 bits), 17)
//    val carry1bit = in Vec(Bits(1 bit), 16)
//    val OUT = out Vec(Bits(16 bits), 17)
//  }
//
//  val Med = Vec.fill(17)(Bits(17 bits))
//  Med(0) := (io.A(0).resize(17 bits).asUInt + io.B(0).resize(17 bits).asUInt).asBits.resize(17 bits)
//  for (i <- 1 until 16) {
//    Med(i) := (io.A(i).resize(17 bits).asUInt + io.B(i).resize(17 bits).asUInt + Med(i-1)(16 downto 16).resize(17 bits).asUInt + io.carry1bit(i-1).asUInt).asBits.resize(17 bits)
//  }
//  Med(16) := (io.B(16).asUInt + Med(15)(16 downto 16).asUInt + io.carry1bit(15).asUInt).asBits.resize(17 bits)
//
//  for (i <- 0 until 17) {
//    io.OUT(i) := Med(i)(15 downto 0)
//  }
//
//}
//
//
//
//case class fmul_mod_combined_new_timing() extends Component {
//
//  val io = new Bundle {
//    val start = in Bool()
//    //    val done = out Bool()
//    val A = in Vec(Bits(16 bits), 16)
//    val B = in Vec(Bits(16 bits), 16)
//    val mode_mod19 = in Bits(1 bits)
//    val size_A = in Bits(4 bits)
//    val cnt_out = out UInt(5 bits)
//    //    val add_out = in Vec(Bits(32 bits), 8)
//    val add_in_A = out Vec(Bits(32 bits), 8)
//    val add_in_B = out Bits(32 bits)
//    val A_reg_modL = out Vec(Bits(16 bits), 32)
//  }
//
//  val clk = in Bool()
//  val rst = in Bool()
//
//  val macs = Vector.fill(16)(fmac_limb())
//  val acc = facc_limbs()
//  val agg = fsum_16()
//
//  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
//  val coreArea = new ClockingArea(clkDomain) {
//
//    val A_reg_mod19 = Vec(Bits(22 bits), 32)
//
//    val A_reg = Vec(Bits(16 bits), 16)
//    val B_reg = Vec(Bits(16 bits), 16)
//
//    val A_mod19 = Vec(Bits(22 bits), 16)
//    val B_mod19 = Vec(Bits(16 bits), 16)
//    val carry_mod19 = Vec(Bits(22 bits), 16)
//
//    val A_modL = Vec(Bits(22 bits), 16)
//    val B_modL = Vec(Bits(16 bits), 16)
//    val carry_modL = Vec(Bits(22 bits), 16)
//
//    val mac_out = Vec(Bits(38 bits), 16)
//    val mul_out = Vec(Bits(38 bits), 16)
//    val carry_reg = Bits(4 bits)
//
//    val mask = Bits(16 bits)
//
//    val cnt = Reg(UInt(5 bits))
//
//    val cnt_fsm = new StateMachine {
//      val cnt_init = new State with EntryPoint
//      val cnt_change = new State
//
//      cnt_init
//        .onEntry {
//          cnt := U(0, 5 bits)
//        }
//        .whenIsActive {
//          when(io.start === True) {
//            goto(cnt_change)
//          }
//        }
//      cnt_change
//        .onEntry {
//          cnt := U(0, 5 bits)
//        }
//        .whenIsActive {
//          cnt := cnt + 1
//          when(io.mode_mod19 === B(1, 1 bit) && cnt === 20) {
//            //            io.done := True
//            goto(cnt_init)
//          }
//          when(io.mode_mod19 === B(0, 1 bit) && cnt === (io.size_A.asUInt.resize(5 bits) + U(2, 5 bits))) {
//            //            io.done := True
//            goto(cnt_init)
//          }
//        }
//    }
//
//    B_reg := RegNext((cnt === 0 && io.start)? io.B | B_reg)
//    A_reg := RegNext((io.mode_mod19 === B(0, 1 bit) && cnt === 0 && io.start)? io.A | A_reg)
//
//    A_reg_mod19(0) := (cnt === 0) ? RegNext(io.A(15)).resize(22 bits) | ((cnt === 18) ? RegNext(acc.io.Out(14 downto 0).resize(22 bits)) | RegNext(acc.io.Out.resize(22 bits)))
//    for (i <- 1 until 16) {
//      A_reg_mod19(i) := (cnt === 0) ? RegNext(io.A(15 - i)).resize(22 bits) | RegNext(A_reg_mod19(i - 1))
//    }
//    for (i <- 16 until 32) {
//      A_reg_mod19(i) := (cnt === 0) ? RegNext(macs(31 - i).io.OUT(21 downto 0).asBits) | RegNext(A_reg_mod19(i - 1))
//    }
//
//    val checksize = Bits(32 bits)
//    //    val checksize_higher = Bits(17 bits)
//    for (i <- 0 until 15) {
//      checksize(i) := (i < io.size_A.asUInt.resize(5 bits))
//      //      if ((checksize(i) == True)) {
//      //        io.A_reg_modL(i) := (cnt <= 1)? B(0, 16 bits) | ((cnt === (i+2)) ? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i)))
//      //      } else {
//      //        io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i)))
//      //      }
//      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((checksize(i) === True)? ((cnt === (i+2))? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i))) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i))))
//    }
//    for (i <- 15 until 32) {
//      checksize(i) := (i > io.size_A.asUInt.resize(5 bits) + U(16, 5 bits))
//      //      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i)))
//      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? ((checksize(i) === True)? B(0, 16 bits) | RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits)))) | RegNext(io.A_reg_modL(i)))
//      //        ((checksize(i) === True)? ((cnt === (i+2))? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i))) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i))))
//    }
//
//
//    //    for (i <- 0 until 15) {
//    //      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (i + 2)) ? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i)))
//    //    }
//    //    for (i <- 15 until 32) {
//    //      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === 17) ? RegNext(agg.io.OUT(i - 15)) | RegNext(io.A_reg_modL(i)))
//    //    }
//
//    for (id <- 0 until 16) {
//      //      println(s"id: $id 15+id: ${15+id} ")
//      B_mod19(id) := (cnt === 0 || cnt === 17) ? B(19, 16 bits) | ((cnt >= 18)? B(0, 16 bits) | B_reg(id))
//      A_mod19(id) := (cnt === 0) ? Cat(io.A(id), B(0, 1 bit)).resize(22 bits) | ((cnt === 17) ? mac_out(id)(36 downto 15) | ((cnt >= 18)? B(0, 22 bits) | A_reg_mod19(16+id).resize(22 bits)))
//      //      mac.io.A := io.A(id).resized
//      carry_mod19(id) := (cnt <= 1) ? B(0, 22 bits) | ((cnt === 17) ? (B(19, 5 bits) & ((mask(id), mask(id), mask(id), mask(id), mask(id)).asBits)).resize(22 bits) | mac_out(id)(37 downto 16))
//
//    }
//
//    A_modL(0) := A_reg(cnt(3 downto 0)).resize(22 bits)
//    B_modL(0) := B_reg(0)
//    carry_modL(0) := B(0, 22 bits)
//
//    for (id <- 1 until 16) {
//      A_modL(id) := A_reg(cnt(3 downto 0)).resize(22 bits)
//      B_modL(id) := B_reg(id)
//      carry_modL(id) := macs(id - 1).io.mulOut(37 downto 16)
//
//    }
//
//    for (id <- 0 until 16) {
//      //      println(s"id: $id 15+id: ${15+id} ")
//      macs(id).io.B := (io.mode_mod19 === B(1, 1 bit)) ? B_mod19(id) | B_modL(id)
//      macs(id).io.A := (io.mode_mod19 === B(1, 1 bit)) ? A_mod19(id) | A_modL(id)
//      //      mac.io.A := io.A(id).resized
//      macs(id).io.Carry := (io.mode_mod19 === B(1, 1 bit)) ? carry_mod19(id) | carry_modL(id)
//
//      agg.io.B(id) := (io.mode_mod19 === B(0, 1 bit)) ? mac_out(id)(15 downto 0).asBits | B(0, 16 bits)
//      agg.io.A(id) := (io.mode_mod19 === B(0, 1 bit)) ? ((cnt <= 1) ? B(0, 16 bits) | RegNext(agg.io.OUT((id + U(1, 5 bits)).resize(5 bits)))) | B(0, 16 bits)
//      agg.io.carry1bit(id) := (io.mode_mod19 === B(0, 1 bit)) ? (mul_out(id)(16 downto 16) ^ mac_out(id)(16 downto 16)) | B(0, 1 bits)
//
//
//      mac_out(id) := RegNext(macs(id).io.OUT)
//      mul_out(id) := RegNext(macs(id).io.mulOut)
//      acc.io.A(id) := (cnt === 17) ? mac_out(id)(14 downto 0).resize(20 bits) | mac_out(id)(15 downto 0).resize(20 bits)
//    }
//    agg.io.B(16) := (io.mode_mod19 === B(0, 1 bit)) ? mac_out(15)(31 downto 16).asBits | B(0, 16 bits)
//
//
//
//    carry_reg := RegNext(acc.io.Out(19 downto 16))
//    acc.io.Carry := (cnt <= 2 || cnt === 18)? B(0, 20 bits) | carry_reg.resize(20 bits)
//
//    mask := (cnt === 17)? ((B(1, 16 bits) << acc.io.Out(18 downto 15).asUInt).asUInt - 1).asBits.resize(16 bits) | B(0, 16 bits)
//    //    mask := (cnt === 17)? ((B(1, 16 bits) << acc.io.Out(19 downto 16).asUInt).asUInt - 1).asBits.resize(16 bits) | B(0, 16 bits)
//
//    println("here a")
//
//    for (id <- 0 until 7) {
//      io.add_in_A(id) := (cnt === 20)? Cat(A_reg_mod19(16 - 2*id)(15 downto 0), A_reg_mod19(17 - 2*id)(15 downto 0)) | B(0, 32 bits)
//    }
//
//    io.add_in_A(7) := (cnt === 20)? Cat(B(0, 1 bits), A_reg_mod19(2)(14 downto 0), A_reg_mod19(3)(15 downto 0)) | B(0, 32 bits)
//
//    println("here B")
//    io.add_in_B := (cnt === 20)? Cat(A_reg_mod19(0)(15 downto 0), A_reg_mod19(1)(15 downto 0)) | B(0, 32 bits)
//    println("here C")
//
//    io.cnt_out := cnt
//
//  }
//
//}
//
//
//case class fmul_mod_combined_new_attempt() extends Component {
//
//  val io = new Bundle {
//    val start = in Bool()
//    //    val done = out Bool()
//    val A = in Vec(Bits(16 bits), 16)
//    val B = in Vec(Bits(16 bits), 16)
//    val mode_mod19 = in Bits(1 bits)
//    val size_A = in Bits(4 bits)
//    val cnt_out = out UInt(5 bits)
//    //    val add_out = in Vec(Bits(32 bits), 8)
//    val add_in_A = out Vec(Bits(32 bits), 8)
//    val add_in_B = out Bits(32 bits)
//    val A_reg_modL = out Vec(Bits(16 bits), 32)
//  }
//
//  val clk = in Bool()
//  val rst = in Bool()
//
//  val macs = Vector.fill(16)(fmac_limb())
//  val acc = facc_limbs()
//  val agg = fsum_16()
//
//  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
//  val coreArea = new ClockingArea(clkDomain) {
//
//    val A_reg_mod19 = Vec(Bits(22 bits), 32)
//
//    val A_reg = Vec(Bits(16 bits), 16)
//    val B_reg = Vec(Bits(16 bits), 16)
//
//    val A_mod19 = Vec(Bits(22 bits), 16)
//    val B_mod19 = Vec(Bits(16 bits), 16)
//    val carry_mod19 = Vec(Bits(22 bits), 16)
//
//    val A_modL = Vec(Bits(22 bits), 16)
//    val B_modL = Vec(Bits(16 bits), 16)
//    val carry_modL = Vec(Bits(22 bits), 16)
//
//    val mac_out = Vec(Bits(38 bits), 16)
//    val mul_out = Vec(Bits(38 bits), 16)
//    val carry_reg = Bits(4 bits)
//
//    val mask = Bits(16 bits)
//
//    val cnt = Reg(UInt(5 bits))
//
//    val cnt_fsm = new StateMachine {
//      val cnt_init = new State with EntryPoint
//      val cnt_change = new State
//
//      cnt_init
//        .onEntry {
//          cnt := U(0, 5 bits)
//        }
//        .whenIsActive {
//          when(io.start === True) {
//            goto(cnt_change)
//          }
//        }
//      cnt_change
//        .onEntry {
//          cnt := U(0, 5 bits)
//        }
//        .whenIsActive {
//          cnt := cnt + 1
//          when(io.mode_mod19 === B(1, 1 bit) && cnt === 20) {
//            //            io.done := True
//            goto(cnt_init)
//          }
//          when(io.mode_mod19 === B(0, 1 bit) && cnt === (io.size_A.asUInt.resize(5 bits) + U(2, 5 bits))) {
//            //            io.done := True
//            goto(cnt_init)
//          }
//        }
//    }
//
//    B_reg := RegNext((cnt === 0 && io.start)? io.B | B_reg)
//    A_reg := RegNext((io.mode_mod19 === B(0, 1 bit) && cnt === 0 && io.start)? io.A | A_reg)
//
//    A_reg_mod19(0) := (cnt === 0) ? RegNext(io.A(15)).resize(22 bits) | ((cnt === 18) ? RegNext(acc.io.Out(14 downto 0).resize(22 bits)) | RegNext(acc.io.Out.resize(22 bits)))
//    for (i <- 1 until 16) {
//      A_reg_mod19(i) := (cnt === 0) ? RegNext(io.A(15 - i)).resize(22 bits) | RegNext(A_reg_mod19(i - 1))
//    }
//    for (i <- 16 until 32) {
//      A_reg_mod19(i) := (cnt === 0) ? RegNext(macs(31 - i).io.OUT(21 downto 0).asBits) | RegNext(A_reg_mod19(i - 1))
//    }
//
//    val checksize = Bits(32 bits)
//    //    val checksize_higher = Bits(17 bits)
//    for (i <- 0 until 15) {
//      checksize(i) := (i < io.size_A.asUInt.resize(5 bits))
//      //      if ((checksize(i) == True)) {
//      //        io.A_reg_modL(i) := (cnt <= 1)? B(0, 16 bits) | ((cnt === (i+2)) ? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i)))
//      //      } else {
//      //        io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i)))
//      //      }
//      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((checksize(i) === True)? ((cnt === (i+2))? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i))) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i))))
//    }
//    for (i <- 15 until 32) {
//      checksize(i) := (i > io.size_A.asUInt.resize(5 bits) + U(16, 5 bits))
//      //      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i)))
//      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? ((checksize(i) === True)? B(0, 16 bits) | RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits)))) | RegNext(io.A_reg_modL(i)))
//      //        ((checksize(i) === True)? ((cnt === (i+2))? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i))) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i))))
//    }
//
//
//    //    for (i <- 0 until 15) {
//    //      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (i + 2)) ? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i)))
//    //    }
//    //    for (i <- 15 until 32) {
//    //      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === 17) ? RegNext(agg.io.OUT(i - 15)) | RegNext(io.A_reg_modL(i)))
//    //    }
//
//    for (id <- 0 until 16) {
//      //      println(s"id: $id 15+id: ${15+id} ")
//      B_mod19(id) := (cnt === 0 || cnt === 17) ? B(19, 16 bits) | ((cnt >= 18)? B(0, 16 bits) | B_reg(id))
//      A_mod19(id) := (cnt === 0) ? Cat(io.A(id), B(0, 1 bit)).resize(22 bits) | ((cnt === 17) ? mac_out(id)(36 downto 15) | ((cnt >= 18)? B(0, 22 bits) | A_reg_mod19(16+id).resize(22 bits)))
//      //      mac.io.A := io.A(id).resized
//      carry_mod19(id) := (cnt <= 1) ? B(0, 22 bits) | ((cnt === 17) ? (B(19, 5 bits) & ((mask(id), mask(id), mask(id), mask(id), mask(id)).asBits)).resize(22 bits) | mac_out(id)(37 downto 16))
//
//    }
//
//    A_modL(0) := A_reg(cnt(3 downto 0)).resize(22 bits)
//    B_modL(0) := B_reg(0)
//    carry_modL(0) := B(0, 22 bits)
//
//    for (id <- 1 until 16) {
//      A_modL(id) := A_reg(cnt(3 downto 0)).resize(22 bits)
//      B_modL(id) := B_reg(id)
//      carry_modL(id) := macs(id - 1).io.mulOut(37 downto 16)
//
//    }
//
//    for (id <- 0 until 16) {
//      //      println(s"id: $id 15+id: ${15+id} ")
//      macs(id).io.B := (io.mode_mod19 === B(1, 1 bit)) ? B_mod19(id) | B_modL(id)
//      macs(id).io.A := (io.mode_mod19 === B(1, 1 bit)) ? A_mod19(id) | A_modL(id)
//      //      mac.io.A := io.A(id).resized
//      macs(id).io.Carry := (io.mode_mod19 === B(1, 1 bit)) ? carry_mod19(id) | carry_modL(id)
//
//      agg.io.B(id) := (io.mode_mod19 === B(0, 1 bit)) ? mac_out(id)(15 downto 0).asBits | B(0, 16 bits)
//      agg.io.A(id) := (io.mode_mod19 === B(0, 1 bit)) ? ((cnt <= 1) ? B(0, 16 bits) | RegNext(agg.io.OUT((id + U(1, 5 bits)).resize(5 bits)))) | B(0, 16 bits)
////      agg.io.carry1bit(id) := (io.mode_mod19 === B(0, 1 bit)) ? (mul_out(id)(16 downto 16) ^ mac_out(id)(16 downto 16)) | B(0, 1 bits)
//      agg.io.carry1bit(id) := (io.mode_mod19 === B(0, 1 bit)) ? (mul_out(id)(16 downto 16) ^ mac_out(id)(16 downto 16)) | B(0, 1 bits)
//
//
//      mac_out(id) := RegNext(macs(id).io.OUT)
//      mul_out(id) := RegNext(macs(id).io.mulOut)
//      acc.io.A(id) := (cnt === 17) ? mac_out(id)(14 downto 0).resize(20 bits) | mac_out(id)(15 downto 0).resize(20 bits)
//    }
//    agg.io.B(16) := (io.mode_mod19 === B(0, 1 bit)) ? mul_out(15)(31 downto 16).asBits | B(0, 16 bits)
//
//
//
//    carry_reg := RegNext(acc.io.Out(19 downto 16))
//    acc.io.Carry := (cnt <= 2 || cnt === 18)? B(0, 20 bits) | carry_reg.resize(20 bits)
//
//    mask := (cnt === 17)? ((B(1, 16 bits) << acc.io.Out(18 downto 15).asUInt).asUInt - 1).asBits.resize(16 bits) | B(0, 16 bits)
//    //    mask := (cnt === 17)? ((B(1, 16 bits) << acc.io.Out(19 downto 16).asUInt).asUInt - 1).asBits.resize(16 bits) | B(0, 16 bits)
//
//    println("here a")
//
//    for (id <- 0 until 7) {
//      io.add_in_A(id) := (cnt === 20)? Cat(A_reg_mod19(16 - 2*id)(15 downto 0), A_reg_mod19(17 - 2*id)(15 downto 0)) | B(0, 32 bits)
//    }
//
//    io.add_in_A(7) := (cnt === 20)? Cat(B(0, 1 bits), A_reg_mod19(2)(14 downto 0), A_reg_mod19(3)(15 downto 0)) | B(0, 32 bits)
//
//    println("here B")
//    io.add_in_B := (cnt === 20)? Cat(A_reg_mod19(0)(15 downto 0), A_reg_mod19(1)(15 downto 0)) | B(0, 32 bits)
//    println("here C")
//
//    io.cnt_out := cnt
//
//  }
//
//}
//
//
//
//case class fmul_mod_combined_1() extends Component {
//
//  val io = new Bundle {
//    val start = in Bool()
//    //    val done = out Bool()
//    val A = in Vec(Bits(16 bits), 16)
//    val B = in Vec(Bits(16 bits), 16)
//    val mode_mod19 = in Bits(1 bits)
//    val size_A = in Bits(4 bits)
//    val cnt_out = out UInt(5 bits)
//    //    val add_out = in Vec(Bits(32 bits), 8)
//    val add_in_A = out Vec(Bits(32 bits), 8)
//    val add_in_B = out Bits(32 bits)
//    val A_reg_modL = out Vec(Bits(16 bits), 32)
//  }
//
//  val clk = in Bool()
//  val rst = in Bool()
//
//  val macs = Vector.fill(16)(fmac_limb())
//  val acc = facc_limbs()
//  val agg = fsum_16()
//
//  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
//  val coreArea = new ClockingArea(clkDomain) {
//
//    val A_reg_mod19 = Vec(Bits(22 bits), 32)
//
//    val A_reg = Vec(Bits(16 bits), 16)
//    val B_reg = Vec(Bits(16 bits), 16)
//
//    val A_mod19 = Vec(Bits(22 bits), 16)
//    val B_mod19 = Vec(Bits(16 bits), 16)
//    val carry_mod19 = Vec(Bits(22 bits), 16)
//
//    val A_modL = Vec(Bits(22 bits), 16)
//    val B_modL = Vec(Bits(16 bits), 16)
//    val carry_modL = Vec(Bits(22 bits), 16)
//
//    val mac_out = Vec(Bits(38 bits), 16)
//    val mul_out = Vec(Bits(38 bits), 16)
//    val carry_reg = Bits(4 bits)
//
//    val mask = Bits(16 bits)
//
//    val cnt = Reg(UInt(5 bits))
//
//    val cnt_fsm = new StateMachine {
//      val cnt_init = new State with EntryPoint
//      val cnt_change = new State
//
//      cnt_init
//        .onEntry {
//          cnt := U(0, 5 bits)
//        }
//        .whenIsActive {
//          when(io.start === True) {
//            goto(cnt_change)
//          }
//        }
//      cnt_change
//        .onEntry {
//          cnt := cnt + 1
//        }
//        .whenIsActive {
//          cnt := cnt + 1
//          when(io.mode_mod19 === B(1, 1 bit) && cnt === 20) {
//            //            io.done := True
//            goto(cnt_init)
//          }
//          when(io.mode_mod19 === B(0, 1 bit) && cnt === (io.size_A.asUInt.resize(5 bits) + U(2, 5 bits))) {
//            //            io.done := True
//            goto(cnt_init)
//          }
//        }
//    }
//
//    B_reg := (cnt === 0)? RegNext(io.B) | RegNext(B_reg)
//    A_reg := (io.mode_mod19 === B(0, 1 bit) && cnt === 0)? RegNext(io.A) | RegNext(A_reg)
//
//    A_reg_mod19(0) := (cnt === 0)? RegNext(io.A(15)).resize(22 bits) | ((cnt === 18)? RegNext(acc.io.Out(14 downto 0).resize(22 bits)) | RegNext(acc.io.Out.resize(22 bits)))
//    for (i <- 1 until 16) {
//      A_reg_mod19(i) := (cnt === 0)? RegNext(io.A(15-i)).resize(22 bits) | RegNext(A_reg_mod19(i-1))
//    }
//    for (i <- 16 until 32) {
//      A_reg_mod19(i) := (cnt === 0)? RegNext(macs(31-i).io.OUT(21 downto 0).asBits) | RegNext(A_reg_mod19(i - 1))
//    }
//
//    val checksize = Bits(32 bits)
//    //    val checksize_higher = Bits(17 bits)
//    for (i <- 0 until 15) {
//      checksize(i) := (i < io.size_A.asUInt.resize(5 bits))
//      //      if ((checksize(i) == True)) {
//      //        io.A_reg_modL(i) := (cnt <= 1)? B(0, 16 bits) | ((cnt === (i+2)) ? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i)))
//      //      } else {
//      //        io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i)))
//      //      }
//      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((checksize(i) === True)? ((cnt === (i+2))? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i))) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i))))
//    }
//    for (i <- 15 until 32) {
//      checksize(i) := (i > io.size_A.asUInt.resize(5 bits) + U(16, 5 bits))
//      //      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i)))
//      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? ((checksize(i) === True)? B(0, 16 bits) | RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits)))) | RegNext(io.A_reg_modL(i)))
//      //        ((checksize(i) === True)? ((cnt === (i+2))? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i))) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i))))
//    }
//
//
//    //    for (i <- 0 until 15) {
//    //      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (i + 2)) ? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i)))
//    //    }
//    //    for (i <- 15 until 32) {
//    //      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === 17) ? RegNext(agg.io.OUT(i - 15)) | RegNext(io.A_reg_modL(i)))
//    //    }
//
//    for (id <- 0 until 16) {
//      //      println(s"id: $id 15+id: ${15+id} ")
//      B_mod19(id) := (cnt === 0 || cnt === 17) ? B(19, 16 bits) | ((cnt >= 18)? B(0, 16 bits) | B_reg(id))
//      A_mod19(id) := (cnt === 0) ? Cat(io.A(id), B(0, 1 bit)).resize(22 bits) | ((cnt === 17) ? mac_out(id)(36 downto 15) | ((cnt >= 18)? B(0, 22 bits) | A_reg_mod19(16+id).resize(22 bits)))
//      //      mac.io.A := io.A(id).resized
//      carry_mod19(id) := (cnt <= 1) ? B(0, 22 bits) | ((cnt === 17) ? (B(19, 5 bits) & ((mask(id), mask(id), mask(id), mask(id), mask(id)).asBits)).resize(22 bits) | mac_out(id)(37 downto 16))
//
//    }
//
//    A_modL(0) := A_reg(cnt(3 downto 0)).resize(22 bits)
//    B_modL(0) := B_reg(0)
//    carry_modL(0) := B(0, 22 bits)
//
//    for (id <- 1 until 16) {
//      A_modL(id) := A_reg(cnt(3 downto 0)).resize(22 bits)
//      B_modL(id) := B_reg(id)
//      carry_modL(id) := macs(id - 1).io.mulOut(37 downto 16)
//
//    }
//
//    for (id <- 0 until 16) {
//      //      println(s"id: $id 15+id: ${15+id} ")
//      macs(id).io.B := (io.mode_mod19 === B(1, 1 bit)) ? B_mod19(id) | B_modL(id)
//      macs(id).io.A := (io.mode_mod19 === B(1, 1 bit)) ? A_mod19(id) | A_modL(id)
//      //      mac.io.A := io.A(id).resized
//      macs(id).io.Carry := (io.mode_mod19 === B(1, 1 bit)) ? carry_mod19(id) | carry_modL(id)
//
//      agg.io.B(id) := (io.mode_mod19 === B(0, 1 bit)) ? mac_out(id)(15 downto 0).asBits | B(0, 16 bits)
//      agg.io.A(id) := (io.mode_mod19 === B(0, 1 bit)) ? ((cnt <= 1) ? B(0, 16 bits) | RegNext(agg.io.OUT((id + U(1, 5 bits)).resize(5 bits)))) | B(0, 16 bits)
//      agg.io.carry1bit(id) := (io.mode_mod19 === B(0, 1 bit)) ? (mul_out(id)(16 downto 16) ^ mac_out(id)(16 downto 16)) | B(0, 1 bits)
//
//
//      mac_out(id) := RegNext(macs(id).io.OUT)
//      mul_out(id) := RegNext(macs(id).io.mulOut)
//      acc.io.A(id) := (cnt === 17) ? mac_out(id)(14 downto 0).resize(20 bits) | mac_out(id)(15 downto 0).resize(20 bits)
//    }
//    agg.io.B(16) := (io.mode_mod19 === B(0, 1 bit)) ? mac_out(15)(31 downto 16).asBits | B(0, 16 bits)
//
//
//
//    carry_reg := RegNext(acc.io.Out(19 downto 16))
//    acc.io.Carry := (cnt <= 2 || cnt === 18)? B(0, 20 bits) | carry_reg.resize(20 bits)
//
//    mask := (cnt === 17)? ((B(1, 16 bits) << acc.io.Out(18 downto 15).asUInt).asUInt - 1).asBits.resize(16 bits) | B(0, 16 bits)
//    //    mask := (cnt === 17)? ((B(1, 16 bits) << acc.io.Out(19 downto 16).asUInt).asUInt - 1).asBits.resize(16 bits) | B(0, 16 bits)
//
//    println("here a")
//
//    for (id <- 0 until 7) {
//      io.add_in_A(id) := (cnt === 20)? Cat(A_reg_mod19(16 - 2*id)(15 downto 0), A_reg_mod19(17 - 2*id)(15 downto 0)) | B(0, 32 bits)
//    }
//
//    io.add_in_A(7) := (cnt === 20)? Cat(B(0, 1 bits), A_reg_mod19(2)(14 downto 0), A_reg_mod19(3)(15 downto 0)) | B(0, 32 bits)
//
//    println("here B")
//    io.add_in_B := (cnt === 20)? Cat(A_reg_mod19(0)(15 downto 0), A_reg_mod19(1)(15 downto 0)) | B(0, 32 bits)
//    println("here C")
//
//    io.cnt_out := cnt
//
//  }
//
//}
//
//
//
//case class fmul_mod_combined() extends Component {
//
//  val io = new Bundle {
//    val start = in Bool()
////    val done = out Bool()
//    val A = in Vec(Bits(16 bits), 16)
//    val B = in Vec(Bits(16 bits), 16)
//    val mode_mod19 = in Bits(1 bits)
//    val size_A = in Bits(4 bits)
//    val cnt_out = out UInt(5 bits)
//    //    val add_out = in Vec(Bits(32 bits), 8)
//    val add_in_A = out Vec(Bits(32 bits), 8)
//    val add_in_B = out Bits(32 bits)
//    val A_reg_modL = out Vec(Bits(16 bits), 32)
//  }
//
//  val clk = in Bool()
//  val rst = in Bool()
//
//  val macs = Vector.fill(16)(fmac_limb())
//  val acc = facc_limbs()
//  val agg = fsum_16()
//
//  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
//  val coreArea = new ClockingArea(clkDomain) {
//
//    val A_reg_mod19 = Vec(Bits(22 bits), 32)
//
//    val A_reg = Vec(Bits(16 bits), 16)
//    val B_reg = Vec(Bits(16 bits), 16)
//
//    val A_mod19 = Vec(Bits(22 bits), 16)
//    val B_mod19 = Vec(Bits(16 bits), 16)
//    val carry_mod19 = Vec(Bits(22 bits), 16)
//
//    val A_modL = Vec(Bits(22 bits), 16)
//    val B_modL = Vec(Bits(16 bits), 16)
//    val carry_modL = Vec(Bits(22 bits), 16)
//
//    val mac_out = Vec(Bits(38 bits), 16)
//    val carry_reg = Bits(4 bits)
//
//    val mask = Bits(16 bits)
//
//    val cnt = Reg(UInt(5 bits))
//
//    val cnt_fsm = new StateMachine {
//      val cnt_init = new State with EntryPoint
//      val cnt_change = new State
//
//      cnt_init
//        .onEntry {
//          cnt := U(0, 5 bits)
//        }
//        .whenIsActive {
//          when(io.start === True) {
//            goto(cnt_change)
//          }
//        }
//      cnt_change
//        .onEntry {
//          cnt := cnt + 1
//        }
//        .whenIsActive {
//          cnt := cnt + 1
//          when(io.mode_mod19 === B(1, 1 bit) && cnt === 20) {
////            io.done := True
//            goto(cnt_init)
//          }
//          when(io.mode_mod19 === B(0, 1 bit) && cnt === (io.size_A.asUInt.resize(5 bits) + U(2, 5 bits))) {
////            io.done := True
//            goto(cnt_init)
//          }
//        }
//    }
//
//    B_reg := (cnt === 0)? RegNext(io.B) | RegNext(B_reg)
//    A_reg := (io.mode_mod19 === B(0, 1 bit) && cnt === 0)? RegNext(io.A) | RegNext(A_reg)
//
//    A_reg_mod19(0) := (cnt === 0)? RegNext(io.A(15)).resize(22 bits) | ((cnt === 18)? RegNext(acc.io.Out(14 downto 0).resize(22 bits)) | RegNext(acc.io.Out.resize(22 bits)))
//    for (i <- 1 until 16) {
//      A_reg_mod19(i) := (cnt === 0)? RegNext(io.A(15-i)).resize(22 bits) | RegNext(A_reg_mod19(i-1))
//    }
//    for (i <- 16 until 32) {
//      A_reg_mod19(i) := (cnt === 0)? RegNext(macs(31-i).io.OUT(21 downto 0).asBits) | RegNext(A_reg_mod19(i - 1))
//    }
//
//    val checksize = Bits(32 bits)
////    val checksize_higher = Bits(17 bits)
//    for (i <- 0 until 15) {
//      checksize(i) := (i < io.size_A.asUInt.resize(5 bits))
////      if ((checksize(i) == True)) {
////        io.A_reg_modL(i) := (cnt <= 1)? B(0, 16 bits) | ((cnt === (i+2)) ? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i)))
////      } else {
////        io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i)))
////      }
//      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((checksize(i) === True)? ((cnt === (i+2))? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i))) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i))))
//    }
//    for (i <- 15 until 32) {
//      checksize(i) := (i > io.size_A.asUInt.resize(5 bits) + U(16, 5 bits))
////      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i)))
//      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? ((checksize(i) === True)? B(0, 16 bits) | RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits)))) | RegNext(io.A_reg_modL(i)))
////        ((checksize(i) === True)? ((cnt === (i+2))? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i))) | ((cnt === (io.size_A.asUInt + U(2, 5 bits))) ? RegNext(agg.io.OUT(i - io.size_A.asUInt.resize(5 bits))) | RegNext(io.A_reg_modL(i))))
//    }
//
//
////    for (i <- 0 until 15) {
////      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === (i + 2)) ? RegNext(agg.io.OUT(0)) | RegNext(io.A_reg_modL(i)))
////    }
////    for (i <- 15 until 32) {
////      io.A_reg_modL(i) := (cnt <= 1) ? B(0, 16 bits) | ((cnt === 17) ? RegNext(agg.io.OUT(i - 15)) | RegNext(io.A_reg_modL(i)))
////    }
//
//    for (id <- 0 until 16) {
//      //      println(s"id: $id 15+id: ${15+id} ")
//      B_mod19(id) := (cnt === 0 || cnt === 17) ? B(19, 16 bits) | ((cnt >= 18)? B(0, 16 bits) | B_reg(id))
//      A_mod19(id) := (cnt === 0) ? Cat(io.A(id), B(0, 1 bit)).resize(22 bits) | ((cnt === 17) ? mac_out(id)(36 downto 15) | ((cnt >= 18)? B(0, 22 bits) | A_reg_mod19(16+id).resize(22 bits)))
//      //      mac.io.A := io.A(id).resized
//      carry_mod19(id) := (cnt <= 1) ? B(0, 22 bits) | ((cnt === 17) ? (B(19, 5 bits) & ((mask(id), mask(id), mask(id), mask(id), mask(id)).asBits)).resize(22 bits) | mac_out(id)(37 downto 16))
//
//    }
//
//    A_modL(0) := A_reg(cnt(3 downto 0)).resize(22 bits)
//    B_modL(0) := B_reg(0)
//    carry_modL(0) := B(0, 22 bits)
//
//    for (id <- 1 until 16) {
//      A_modL(id) := A_reg(cnt(3 downto 0)).resize(22 bits)
//      B_modL(id) := B_reg(id)
//      carry_modL(id) := macs(id - 1).io.OUT(37 downto 16)
//
//    }
//
//    for (id <- 0 until 16) {
//      //      println(s"id: $id 15+id: ${15+id} ")
//      macs(id).io.B := (io.mode_mod19 === B(1, 1 bit)) ? B_mod19(id) | B_modL(id)
//      macs(id).io.A := (io.mode_mod19 === B(1, 1 bit)) ? A_mod19(id) | A_modL(id)
//      //      mac.io.A := io.A(id).resized
//      macs(id).io.Carry := (io.mode_mod19 === B(1, 1 bit)) ? carry_mod19(id) | carry_modL(id)
//
//      agg.io.B(id) := (io.mode_mod19 === B(0, 1 bit)) ? mac_out(id)(15 downto 0).asBits | B(0, 16 bits)
//      agg.io.A(id) := (io.mode_mod19 === B(0, 1 bit)) ? ((cnt <= 1) ? B(0, 16 bits) | RegNext(agg.io.OUT((id + U(1, 5 bits)).resize(5 bits)))) | B(0, 16 bits)
//
//
//      mac_out(id) := RegNext(macs(id).io.OUT)
//      acc.io.A(id) := (cnt === 17) ? mac_out(id)(14 downto 0).resize(20 bits) | mac_out(id)(15 downto 0).resize(20 bits)
//    }
//    agg.io.B(16) := (io.mode_mod19 === B(0, 1 bit)) ? mac_out(15)(31 downto 16).asBits | B(0, 16 bits)
//
//
//
//    carry_reg := RegNext(acc.io.Out(19 downto 16))
//    acc.io.Carry := (cnt <= 2 || cnt === 18)? B(0, 20 bits) | carry_reg.resize(20 bits)
//
//    mask := (cnt === 17)? ((B(1, 16 bits) << acc.io.Out(18 downto 15).asUInt).asUInt - 1).asBits.resize(16 bits) | B(0, 16 bits)
//    //    mask := (cnt === 17)? ((B(1, 16 bits) << acc.io.Out(19 downto 16).asUInt).asUInt - 1).asBits.resize(16 bits) | B(0, 16 bits)
//
//    println("here a")
//
//    for (id <- 0 until 7) {
//      io.add_in_A(id) := (cnt === 20)? Cat(A_reg_mod19(16 - 2*id)(15 downto 0), A_reg_mod19(17 - 2*id)(15 downto 0)) | B(0, 32 bits)
//    }
//
//    io.add_in_A(7) := (cnt === 20)? Cat(B(0, 1 bits), A_reg_mod19(2)(14 downto 0), A_reg_mod19(3)(15 downto 0)) | B(0, 32 bits)
//
//    println("here B")
//    io.add_in_B := (cnt === 20)? Cat(A_reg_mod19(0)(15 downto 0), A_reg_mod19(1)(15 downto 0)) | B(0, 32 bits)
//    println("here C")
//
//    io.cnt_out := cnt
//
//  }
//
//}
//
//
//
//
//object ModMulWrapGen{
//  def main(args: Array[String]) {
//    val config = SpinalConfig(targetDirectory = "verilog_outputs")
//    config.generateVerilog({
//      val toplevel = new fmul_wrapper()
//      toplevel
//    })
//  }
//}
//
