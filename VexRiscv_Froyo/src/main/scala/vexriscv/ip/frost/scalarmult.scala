package vexriscv.ip.frost

import scala.util.Random

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.amba4.axilite._
import spinal.lib.fsm._

case class scalar_mult() extends Component {

  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()
    val ready = in Bool()

    val wrCheck = out Bool()


    //mode: 0000 - scalarmult, 0001 - Point Addition, 1000 - mulmod 252, 1001 - mulfull, 1010 - mod 252
    //mode: 0100 - mul25519, 0101 - add25519, 0110 - sub25519
    val mode = in Bits(4 bits)
    val size_A1 = in Bits(4 bits)
    val size_A2 = in Bits(4 bits)

    val offsetWr = in Bits(8 bits)


    val rdAddrPXY = out Bits (8 bits)

    val wrAddrPXY = out Bits (8 bits)


    val memP_X = in Bits (256 bits)
    val memP_Y = in Bits (256 bits)


    val readEnable = out Bool()
    val writeEnable = out Bool()


    val wrMem_X = out Bits(256 bits)
    val wrMem_Y = out Bits(256 bits)

  }

  val clk = in Bool()
  val rst = in Bool()

  val p3toCached = p3_to_cached()
  val p1p1top3 = p1p1_to_p3()
  val p3Double = p3_dbl()
  val addCached = add_cached()
  val parseScalar = parse_scalar()
  val negT2D = neg_T2D()
  val mul25519 = mulmod25519()
  val add25519 = addmod25519()
  val sub25519 = submod25519()

  val mul512Inst = mul512()
  val reduceLInst = reduceL()

  val addInst1 = fsum_mod_wrapper()
  val subInst1 = fsub_mod_wrapper()
  val addInst2 = fsum_mod_wrapper()
  val subInst2 = fsub_mod_wrapper()
  val mulInst1 = fmul_wrapper()
  //  val addInst2 = fsum_mod_wrapper()
  //  val subInst2 = fsub_mod_wrapper()
  val mulInst2 = fmul_wrapper()


  //  addInst1.io.A := addCached.io.add1_A
  //  addInst1.io.B := addCached.io.add1_B
  //  addInst1.io.start := addCached.io.fadd1_start
  addInst1.io.modeMod := (io.mode(3 downto 3) === 0)? True | False
  //  addInst1.io.carryIn := B(0, 1 bits)
  addInst1.clk := clk
  addInst1.rst := rst

  //  subInst1.io.A := addCached.io.sub1_A
  //  subInst1.io.B := addCached.io.sub1_B
  //  subInst1.io.start := addCached.io.fsub1_start
  subInst1.io.modeMod := (io.mode(3 downto 3) === 0)? True | False
  //  subInst1.io.carryIn := B(0, 1 bits)
  subInst1.clk := clk
  subInst1.rst := rst

  //  mulInst1.io.A := addCached.io.mul1_A
  //  mulInst1.io.B := addCached.io.mul1_B
  //  mulInst1.io.start := addCached.io.fmul1_start
  mulInst1.io.mode_mod19 := (io.mode(3 downto 3) === 0)? B(1, 1 bit) | B(0, 1 bit)
  mulInst1.io.size_A := io.size_A1
  mulInst1.clk := clk
  mulInst1.rst := rst

  //  addInst2.io.A := addCached.io.add2_A
  //  addInst2.io.B := addCached.io.add2_B
  //  addInst2.io.start := addCached.io.fadd1_start
  addInst2.io.modeMod := (io.mode(3 downto 3) === 0)? True | False
  addInst2.io.carryIn := B(0, 1 bits)
  addInst2.clk := clk
  addInst2.rst := rst

  //  subInst2.io.A := addCached.io.sub2_A
  //  subInst2.io.B := addCached.io.sub2_B
  //  subInst2.io.start := addCached.io.fsub1_start
  subInst2.io.modeMod := (io.mode(3 downto 3) === 0)? True | False
  subInst2.io.carryIn := B(0, 1 bits)
  subInst2.clk := clk
  subInst2.rst := rst

  //  mulInst2.io.A := addCached.io.mul2_A
  //  mulInst2.io.B := addCached.io.mul2_B
  //  mulInst2.io.start := addCached.io.fmul2_start
  mulInst2.io.mode_mod19 := (io.mode(3 downto 3) === 0)? B(1, 1 bit) | B(0, 1 bit)
  mulInst2.io.size_A := io.size_A2
  mulInst2.clk := clk
  mulInst2.rst := rst


  p3toCached.io.add1_OUT := addInst1.io.OUT
  p3toCached.io.sub1_OUT := subInst1.io.OUT
  p3toCached.io.mul1_OUT := mulInst1.io.OUT
  p3toCached.io.mulDone := mulInst1.io.done
  p3toCached.io.d2 := B"256'x2406d9dc56dffce7198e80f2eef3d13000e0149a8283b156ebd69b9426b2f159"
  //  p3toCached.io.d2 := B"256'x0"
  p3toCached.clk := clk
  p3toCached.rst := rst

  p3Double.io.addDone := addInst1.io.done
  p3Double.io.subDone := subInst1.io.done
  p3Double.io.mulDone := mulInst1.io.done
  p3Double.io.add1_OUT := addInst1.io.OUT
  p3Double.io.sub1_OUT := subInst1.io.OUT
  p3Double.io.mul1_OUT := mulInst1.io.OUT
  p3Double.io.mul2_OUT := mulInst2.io.OUT
  p3Double.clk := clk
  p3Double.rst := rst

  p1p1top3.io.mulDone := mulInst1.io.done
  p1p1top3.io.mul1_OUT := mulInst1.io.OUT
  p1p1top3.io.mul2_OUT := mulInst2.io.OUT
  p1p1top3.clk := clk
  p1p1top3.rst := rst

  addCached.io.addDone := addInst1.io.done
  addCached.io.subDone := subInst1.io.done
  addCached.io.mulDone := mulInst1.io.done
  addCached.io.add1_OUT := addInst1.io.OUT
  addCached.io.sub1_OUT := subInst1.io.OUT
  addCached.io.mul1_OUT := mulInst1.io.OUT
  addCached.io.add2_OUT := addInst2.io.OUT
  addCached.io.sub2_OUT := subInst2.io.OUT
  addCached.io.mul2_OUT := mulInst2.io.OUT
  addCached.clk := clk
  addCached.rst := rst

  negT2D.io.subDone := subInst1.io.done
  negT2D.io.sub1_OUT := subInst1.io.OUT
  negT2D.clk := clk
  negT2D.rst := rst

  mul25519.io.mulDone := mulInst1.io.done
  mul25519.io.mul1_OUT := mulInst1.io.OUT
  mul25519.io.mul2_OUT := mulInst2.io.OUT
  mul25519.clk := clk
  mul25519.rst := rst

  add25519.io.addDone := addInst1.io.done
  add25519.io.add1_OUT := addInst1.io.OUT
  add25519.io.add2_OUT := addInst2.io.OUT
  add25519.clk := clk
  add25519.rst := rst

  sub25519.io.subDone := subInst1.io.done
  sub25519.io.sub1_OUT := subInst1.io.OUT
  sub25519.io.sub2_OUT := subInst2.io.OUT
  sub25519.clk := clk
  sub25519.rst := rst


  mul512Inst.io.mulDone := mulInst1.io.done
  mul512Inst.io.mul1_OUT := mulInst1.io.OUT512
  mul512Inst.io.mul2_OUT := mulInst2.io.OUT512
  mul512Inst.clk := clk
  mul512Inst.rst := rst

  reduceLInst.io.add1Done := addInst1.io.done
  reduceLInst.io.add2Done := addInst2.io.done
  reduceLInst.io.sub1Done := subInst1.io.done
  reduceLInst.io.sub2Done := subInst2.io.done
  reduceLInst.io.mul1Done := mulInst1.io.done
  reduceLInst.io.mul2Done := mulInst2.io.done
  reduceLInst.io.add1_OUT := addInst1.io.OUT
  reduceLInst.io.add1_carryOUT := addInst1.io.carryOut
  reduceLInst.io.sub1_OUT := subInst1.io.OUT
  reduceLInst.io.sub1_carryOUT := subInst1.io.carryOut
  reduceLInst.io.mul1_OUT := mulInst1.io.OUT
  reduceLInst.io.mul1_512 := mulInst1.io.OUT512
  reduceLInst.io.add2_OUT := addInst2.io.OUT
  reduceLInst.io.sub2_OUT := subInst2.io.OUT
  reduceLInst.io.mul2_OUT := mulInst2.io.OUT
  reduceLInst.io.mul2_512 := mulInst2.io.OUT512
  reduceLInst.clk := clk
  reduceLInst.rst := rst


  //  io.r_YplusX := p3toCached.io.r_YplusX
  //  io.r_YminusX := p3toCached.io.r_YminusX
  //  //  io.r_Z := p3toCached.io.r_Z
  //  io.r_T2d := p3toCached.io.r_T2d
  //
  //  //  io.done := p3toCached.io.T2d_done
  //
  //
  //  io.r_X := addCached.io.r_X
  //  io.r_Y := addCached.io.r_Y
  //  io.r_Z := addCached.io.r_Z
  //  io.r_T := addCached.io.r_T

  //  io.done := addCached.io.done

  val clkDomain = ClockDomain(clk, rst, config = ClockDomainConfig(resetKind = SYNC))
  val coreArea = new ClockingArea(clkDomain) {

    val startReg = Reg(Bool())
    val allDone = Reg(Bool())

    val op_X = Reg(Bits(256 bits))
    val op_Y = Reg(Bits(256 bits))
    val op_Z = Reg(Bits(256 bits))
    val op_T = Reg(Bits(256 bits))

    val oq_X = Reg(Bits(256 bits))
    val oq_Y = Reg(Bits(256 bits))
    val oq_Z = Reg(Bits(256 bits))
    val oq_T = Reg(Bits(256 bits))

    val int_X = Reg(Bits(256 bits))
    val int_Y = Reg(Bits(256 bits))
    val int_Z = Reg(Bits(256 bits))
    val int_T = Reg(Bits(256 bits))

    val scalar_A = Reg(Bits(256 bits))

    val rdStart = Bool()
    val wrStart = Bool()

    val rdStartParse = Bool()
    val parseStart = Reg(Bool())

    val flag = Reg(Bool())

    val wrBit = Bits(1 bit)

    val cntInit = Reg(UInt(4 bits))
    val cntLoop = Reg(UInt(6 bits))
    val cntInLoop = Reg(UInt(3 bits))

    val loopMode = Reg(Bits(2 bits))

    val cntRdRetrieve = Reg(UInt(3 bits))
    val cntRd1Retrieve = Reg(UInt(3 bits))
    val cntWrRetrieve = Reg(UInt(3 bits))


    val tmp_X = Bits(256 bits)
    val tmp_Y = Bits(256 bits)
    val tmp_Z = Bits(256 bits)
    val tmp_T = Bits(256 bits)

    io.wrCheck := (io.mode(3 downto 3) === 1 || io.mode(2 downto 2) === 1 || io.mode === 1)? True | RegNext(RegNext(loopMode(0 downto 0).asBool))



    val rdFSM = new StateMachine {
      val initRdST = new State with EntryPoint
      val rdST1 = new State
      val rdParseST = new State

      io.rdAddrPXY := B(0, 8 bits)

      io.wrAddrPXY := B(0, 8 bits)

      scalar_A := scalar_A


      io.readEnable := False

      parseStart := False

      int_X := int_X
      int_Y := int_Y
      int_Z := int_Z
      int_T := int_T

      initRdST
        .whenIsActive {
          cntRdRetrieve := 0
          when(rdStart) {
            goto(rdST1)
          }
          when(rdStartParse) {
            goto(rdParseST)
          }
        }
      rdST1
        .onEntry{
          cntRdRetrieve := cntRdRetrieve + 1

        }
        .whenIsActive {
          //          io.readEnable := True
          cntRdRetrieve := cntRdRetrieve + 1
          when(cntRdRetrieve === 1) {
            io.rdAddrPXY := (loopMode === 0)?(cntInit + 2).asBits.resize(8 bits) | (Cat(parseScalar.io.e(cntLoop)(3 downto 0),B(1, 1 bit)).asUInt + 14).asBits.resize(8 bits)
            io.readEnable := True
            goto(rdST1)
          }
          when(cntRdRetrieve === 2) {
            io.rdAddrPXY := (loopMode === 0)?(cntInit + 1).asBits.resize(8 bits) | (Cat(parseScalar.io.e(cntLoop)(3 downto 0),B(0, 1 bit)).asUInt + 14).asBits.resize(8 bits)
            int_Z := io.memP_X
            int_T := io.memP_Y
            io.readEnable := True
            goto(rdST1)
          }
          when(cntRdRetrieve === 3) {
            int_X := io.memP_X
            int_Y := io.memP_Y
            io.readEnable := False
            goto(initRdST)
          }
        }
      rdParseST
        .onEntry{
          cntRdRetrieve := cntRdRetrieve + 1
        }
        .whenIsActive{
          cntRdRetrieve := cntRdRetrieve + 1
          when(cntRdRetrieve === 1) {
            io.rdAddrPXY := B(12, 8 bits)
            io.readEnable := True
            goto(rdParseST)
          }
          when(cntRdRetrieve === 2) {
            scalar_A := io.memP_Y
            io.readEnable := False
            parseStart := True
            goto(initRdST)
          }
        }
    }

    val wrFSM = new StateMachine {
      val initWrST = new State with EntryPoint
      val wrST1 = new State
      val wrST2 = new State
      val wrST512 = new State

      io.wrMem_X := B(0, 256 bits)
      io.wrMem_Y := B(0, 256 bits)
      //      io.wrMem_Z := B(0, 256 bits)
      //      io.wrMem_T := B(0, 256 bits)

      io.writeEnable := False

      initWrST
        .whenIsActive {
          cntWrRetrieve := 0
          when(wrStart) {
            when (io.mode(3 downto 3) === 1){
              goto(wrST512)
            } elsewhen (io.mode(2 downto 2) === 1) {
              goto(wrST512)
            } otherwise {
              goto(wrST1)
            }

          }
        }
      wrST1
        .onEntry {
          cntWrRetrieve := cntWrRetrieve + 1

        }
        .whenIsActive {
          cntWrRetrieve := cntWrRetrieve + 1
          io.writeEnable := True
          when(cntWrRetrieve === 1) {
            //            io.wrAddrPXY := (8 + (cntInit << 1)).asBits.resize(8 bits)
            io.wrAddrPXY := (RegNext(RegNext(loopMode)) === 1 || io.mode === 1)? (io.offsetWr.asUInt + 1).asBits | Cat(wrBit, ((cntInit << 1) + 1).resize(4 bits)).asBits.resize(8 bits)
            io.wrMem_X := oq_Z
            io.wrMem_Y := oq_T
            goto(wrST1)
          }
          when(cntWrRetrieve === 2) {
            //            io.wrAddrPXY := (8 + (cntInit << 1) + 1).asBits.resize(8 bits)
            io.wrAddrPXY := (RegNext(RegNext(loopMode)) === 1 || io.mode === 1)? io.offsetWr | Cat(wrBit, (cntInit << 1).resize(4 bits)).asBits.resize(8 bits)
            io.wrMem_X := oq_X
            io.wrMem_Y := oq_Y
            goto(initWrST)
          }
        }
      wrST512
        .onEntry{
          cntWrRetrieve := cntWrRetrieve + 1
        }
        .whenIsActive {
          cntWrRetrieve := cntWrRetrieve + 1
          io.writeEnable := True
          when(cntWrRetrieve === 1) {
            io.wrAddrPXY := io.offsetWr
            io.wrMem_X := oq_X
            io.wrMem_Y := oq_Y
            goto(initWrST)
          }
        }
    }

    val opsFSM = new StateMachine {
      val initST = new State with EntryPoint
      val retrieveData = new State
      val mul512ST = new State
      val reduceLST = new State
      val mul25519ST = new State
      val add25519ST = new State
      val sub25519ST = new State
      val p3ToCachedST = new State
      val p1p1top3ST = new State
      val p3DoubleST = new State
      val addCachedST = new State
      val loopReadST = new State
      val negT2DST = new State
      val doneST = new State

      rdStart := False
      wrStart := False

      rdStartParse := False

      allDone := allDone
      io.done := RegNext(allDone)

      p3toCached.io.p_X := B(0, 256 bits)
      p3toCached.io.p_Y := B(0, 256 bits)
      p3toCached.io.p_Z := B(0, 256 bits)
      p3toCached.io.p_T := B(0, 256 bits)
      p3toCached.io.start := False

      p3Double.io.p_X := B(0, 256 bits)
      p3Double.io.p_Y := B(0, 256 bits)
      p3Double.io.p_Z := B(0, 256 bits)
      p3Double.io.p_T := B(0, 256 bits)
      p3Double.io.start := False

      p1p1top3.io.p_X := B(0, 256 bits)
      p1p1top3.io.p_Y := B(0, 256 bits)
      p1p1top3.io.p_Z := B(0, 256 bits)
      p1p1top3.io.p_T := B(0, 256 bits)
      p1p1top3.io.start := False

      addCached.io.p_X := B(0, 256 bits)
      addCached.io.p_Y := B(0, 256 bits)
      addCached.io.p_Z := B(0, 256 bits)
      addCached.io.p_T := B(0, 256 bits)
      addCached.io.q_YplusX := B(0, 256 bits)
      addCached.io.q_YminusX := B(0, 256 bits)
      addCached.io.q_Z := B(0, 256 bits)
      addCached.io.q_T2D := B(0, 256 bits)
      addCached.io.start := False

      mul25519.io.p_X := B(0, 256 bits)
      mul25519.io.p_Y := B(0, 256 bits)
      mul25519.io.p_Z := B(0, 256 bits)
      mul25519.io.p_T := B(0, 256 bits)
      mul25519.io.start := False

      add25519.io.p_X := B(0, 256 bits)
      add25519.io.p_Y := B(0, 256 bits)
      add25519.io.p_Z := B(0, 256 bits)
      add25519.io.p_T := B(0, 256 bits)
      add25519.io.start := False

      sub25519.io.p_X := B(0, 256 bits)
      sub25519.io.p_Y := B(0, 256 bits)
      sub25519.io.p_Z := B(0, 256 bits)
      sub25519.io.p_T := B(0, 256 bits)
      sub25519.io.start := False

      negT2D.io.T2D := B(0, 256 bits)
      negT2D.io.start := False

      mul512Inst.io.p_X := B(0, 256 bits)
      mul512Inst.io.p_Y := B(0, 256 bits)
      mul512Inst.io.size_A1_in := B(15, 4 bits)
      mul512Inst.io.size_A2_in := B(15, 4 bits)
      mul512Inst.io.start := False

      reduceLInst.io.start := False
      reduceLInst.io.p_X := B(0, 256 bits)
      reduceLInst.io.p_Y := B(0, 256 bits)

      op_X := op_X
      op_Y := op_Y
      op_Z := op_Z
      op_T := op_T

      oq_X := oq_X
      oq_Y := oq_Y
      oq_Z := oq_Z
      oq_T := oq_T

      tmp_X := B(0, 256 bits)
      tmp_Y := B(0, 256 bits)
      tmp_Z := B(0, 256 bits)
      tmp_T := B(0, 256 bits)

      addInst1.io.A := B(0, 256 bits)
      addInst1.io.B := B(0, 256 bits)
      addInst1.io.carryIn := B(0, 1 bit)
      subInst1.io.A := B(0, 256 bits)
      subInst1.io.B := B(0, 256 bits)
      subInst1.io.carryIn := B(0, 1 bit)
      mulInst1.io.A := B(0, 256 bits)
      mulInst1.io.B := B(0, 256 bits)
      addInst2.io.A := B(0, 256 bits)
      addInst2.io.B := B(0, 256 bits)
      subInst2.io.A := B(0, 256 bits)
      subInst2.io.B := B(0, 256 bits)
      mulInst2.io.A := B(0, 256 bits)
      mulInst2.io.B := B(0, 256 bits)

      addInst1.io.start := False
      addInst2.io.start := False
      subInst1.io.start := False
      subInst2.io.start := False
      mulInst1.io.start := False
      mulInst2.io.start := False

      wrBit := 0
      flag := flag

      startReg := False


      initST
        .onEntry {
          cntInit := U(0, 4 bits)
          cntLoop := U(63, 6 bits)
          cntInLoop := U(0, 3 bits)
          flag := False
        }
        .whenIsActive {
          cntRd1Retrieve := 0
          startReg := io.start
          allDone := False
          when(startReg === True) {
            goto(retrieveData)
          }
        }
      retrieveData
        .onEntry {
          cntRd1Retrieve := cntRd1Retrieve + 1
          io.rdAddrPXY := (cntInit << 1).asBits.resize(8 bits)
          io.readEnable := True
        }
        .whenIsActive{
          loopMode := B(0, 2 bits)
          cntRd1Retrieve := cntRd1Retrieve + 1
          when(cntRd1Retrieve === 1) {
            io.rdAddrPXY := ((cntInit << 1) + 1).asBits.resize(8 bits)
            io.readEnable := True
            oq_X := io.memP_X
            oq_Y := io.memP_Y
            when(io.mode(3 downto 0) === B(8, 4 bits) || io.mode(3 downto 0) === B(9, 4 bits)) {
              goto(mul512ST)
            } elsewhen(io.mode(3 downto 0) === B(10, 4 bits)){
              goto(reduceLST)
            }
          }
          when(cntRd1Retrieve === 2) {
            oq_Z := io.memP_X
            oq_T := io.memP_Y
            when (io.mode(3 downto 0) === 0){
              goto(p3ToCachedST)
            } elsewhen(io.mode(3 downto 0) === 1) {
//              rdStart := True
              goto(p3ToCachedST)
            } elsewhen(io.mode(3 downto 0) === 4){
              goto(mul25519ST)
            } elsewhen(io.mode(3 downto 0) === 5){
              goto(add25519ST)
            } elsewhen(io.mode(3 downto 0) === 6) {
              goto(sub25519ST)
            } otherwise {
              goto(initST)
            }

          }
        }
      mul512ST
        .onEntry {
          mul512Inst.io.p_X := io.memP_X
          mul512Inst.io.p_Y := io.memP_Y
          mul512Inst.io.size_A1_in := io.size_A1
          mul512Inst.io.size_A2_in := io.size_A2
          mul512Inst.io.start := True

          mulInst1.io.A := mul512Inst.io.mul1_A
          mulInst1.io.B := mul512Inst.io.mul1_B
          mulInst1.io.start := mul512Inst.io.fmul1_start
        }
        .whenIsActive{
          when(mul512Inst.io.done === True) {
            oq_X := mul512Inst.io.r_X
            oq_Y := mul512Inst.io.r_Y
            when (io.mode === B(8, 4 bits)){
              goto(reduceLST)
            } elsewhen(io.mode === B(9, 4 bits)) {
              wrStart := True
              allDone := True
              goto(doneST)
            }

          }
        }
      reduceLST
        .onEntry{
          reduceLInst.io.start := True
          when (io.mode === B(8, 4 bits)) {
            reduceLInst.io.p_X := mul512Inst.io.r_X
            reduceLInst.io.p_Y := mul512Inst.io.r_Y
          } elsewhen(io.mode === B(10, 4 bits)) {
            reduceLInst.io.p_X := io.memP_X
            reduceLInst.io.p_Y := io.memP_Y
          }

          addInst1.io.A := reduceLInst.io.add1_A
          addInst1.io.B := reduceLInst.io.add1_B
          addInst1.io.carryIn := reduceLInst.io.add1_carryIn
          subInst1.io.A := reduceLInst.io.sub1_A
          subInst1.io.B := reduceLInst.io.sub1_B
          subInst1.io.carryIn := reduceLInst.io.sub1_carryIn
          mulInst1.io.A := reduceLInst.io.mul1_A
          mulInst1.io.B := reduceLInst.io.mul1_B
          mulInst1.io.size_A := reduceLInst.io.mul1_sizeA

          addInst2.io.A := reduceLInst.io.add2_A
          addInst2.io.B := reduceLInst.io.add2_B
          subInst2.io.A := reduceLInst.io.sub2_A
          subInst2.io.B := reduceLInst.io.sub2_B
          mulInst2.io.A := reduceLInst.io.mul2_A
          mulInst2.io.B := reduceLInst.io.mul2_B
          mulInst2.io.size_A := reduceLInst.io.mul2_sizeA

          addInst1.io.start := reduceLInst.io.fadd1_start
          subInst1.io.start := reduceLInst.io.fsub1_start
          mulInst1.io.start := reduceLInst.io.fmul1_start

          addInst2.io.start := reduceLInst.io.fadd2_start
          subInst2.io.start := reduceLInst.io.fsub2_start
          mulInst2.io.start := reduceLInst.io.fmul2_start

        }
        .whenIsActive{
          addInst1.io.A := reduceLInst.io.add1_A
          addInst1.io.B := reduceLInst.io.add1_B
          addInst1.io.carryIn := reduceLInst.io.add1_carryIn
          subInst1.io.A := reduceLInst.io.sub1_A
          subInst1.io.B := reduceLInst.io.sub1_B
          subInst1.io.carryIn := reduceLInst.io.sub1_carryIn
          mulInst1.io.A := reduceLInst.io.mul1_A
          mulInst1.io.B := reduceLInst.io.mul1_B
          mulInst1.io.size_A := reduceLInst.io.mul1_sizeA

          addInst2.io.A := reduceLInst.io.add2_A
          addInst2.io.B := reduceLInst.io.add2_B
          subInst2.io.A := reduceLInst.io.sub2_A
          subInst2.io.B := reduceLInst.io.sub2_B
          mulInst2.io.A := reduceLInst.io.mul2_A
          mulInst2.io.B := reduceLInst.io.mul2_B
          mulInst2.io.size_A := reduceLInst.io.mul2_sizeA

          addInst1.io.start := reduceLInst.io.fadd1_start
          subInst1.io.start := reduceLInst.io.fsub1_start
          mulInst1.io.start := reduceLInst.io.fmul1_start

          addInst2.io.start := reduceLInst.io.fadd2_start
          subInst2.io.start := reduceLInst.io.fsub2_start
          mulInst2.io.start := reduceLInst.io.fmul2_start
          when(reduceLInst.io.done === True){
            oq_X := reduceLInst.io.r_X
            oq_Y := reduceLInst.io.r_X
            wrStart := True
            allDone := True
            goto(doneST)
          }
        }

      mul25519ST
        .onEntry{
          wrBit := 0

          mul25519.io.p_X := oq_X
          mul25519.io.p_Y := oq_Y
          mul25519.io.p_Z := io.memP_X
          mul25519.io.p_T := io.memP_Y
          mul25519.io.start := True

          mulInst1.io.A := mul25519.io.mul1_A
          mulInst1.io.B := mul25519.io.mul1_B

          mulInst2.io.A := mul25519.io.mul2_A
          mulInst2.io.B := mul25519.io.mul2_B

          mulInst1.io.start := mul25519.io.fmul1_start
          mulInst2.io.start := mul25519.io.fmul2_start
        }
        .whenIsActive{
          mulInst1.io.A := mul25519.io.mul1_A
          mulInst1.io.B := mul25519.io.mul1_B

          mulInst2.io.A := mul25519.io.mul2_A
          mulInst2.io.B := mul25519.io.mul2_B

          mulInst1.io.start := mul25519.io.fmul1_start
          mulInst2.io.start := mul25519.io.fmul2_start

          when(mul25519.io.done === True){
            wrStart := True
            oq_X := mul25519.io.r_X
            oq_Y := mul25519.io.r_Y
            allDone := True
            goto(doneST)
          }

        }

      add25519ST
        .onEntry{
          wrBit := 0

          add25519.io.p_X := oq_X
          add25519.io.p_Y := oq_Y
          add25519.io.p_Z := io.memP_X
          add25519.io.p_T := io.memP_Y
          add25519.io.start := True

          addInst1.io.A := add25519.io.add1_A
          addInst1.io.B := add25519.io.add1_B

          addInst2.io.A := add25519.io.add2_A
          addInst2.io.B := add25519.io.add2_B

          addInst1.io.start := add25519.io.fadd1_start
          addInst2.io.start := add25519.io.fadd2_start
        }
        .whenIsActive{
          addInst1.io.A := add25519.io.add1_A
          addInst1.io.B := add25519.io.add1_B

          addInst2.io.A := add25519.io.add2_A
          addInst2.io.B := add25519.io.add2_B

          addInst1.io.start := add25519.io.fadd1_start
          addInst2.io.start := add25519.io.fadd2_start

          when(add25519.io.done === True){
            wrStart := True
            oq_X := add25519.io.r_X
            oq_Y := add25519.io.r_Y
            allDone := True
            goto(doneST)
          }

        }

      sub25519ST
        .onEntry{
          wrBit := 0

          sub25519.io.p_X := oq_X
          sub25519.io.p_Y := oq_Y
          sub25519.io.p_Z := io.memP_X
          sub25519.io.p_T := io.memP_Y
          sub25519.io.start := True

          subInst1.io.A := sub25519.io.sub1_A
          subInst1.io.B := sub25519.io.sub1_B

          subInst2.io.A := sub25519.io.sub2_A
          subInst2.io.B := sub25519.io.sub2_B

          subInst1.io.start := sub25519.io.fsub1_start
          subInst2.io.start := sub25519.io.fsub2_start
        }
        .whenIsActive{
          subInst1.io.A := sub25519.io.sub1_A
          subInst1.io.B := sub25519.io.sub1_B

          subInst2.io.A := sub25519.io.sub2_A
          subInst2.io.B := sub25519.io.sub2_B

          subInst1.io.start := sub25519.io.fsub1_start
          subInst2.io.start := sub25519.io.fsub2_start

          when(sub25519.io.done === True){
            wrStart := True
            oq_X := sub25519.io.r_X
            oq_Y := sub25519.io.r_Y
            allDone := True
            goto(doneST)
          }

        }

      p3ToCachedST
        .onEntry {
          wrBit := 0

          when(cntInit === 0) {
            p3toCached.io.p_X := oq_X
            p3toCached.io.p_Y := oq_Y
            p3toCached.io.p_Z := io.memP_X
            p3toCached.io.p_T := io.memP_Y
            p3toCached.io.start := True
            op_X := oq_X
            op_Y := oq_Y
            op_Z := io.memP_X
            op_T := io.memP_Y
            int_X := oq_X
            int_Y := oq_Y
            int_Z := io.memP_X
            int_T := io.memP_Y
          } otherwise {
            p3toCached.io.p_X := p1p1top3.io.r_X
            p3toCached.io.p_Y := p1p1top3.io.r_Y
            p3toCached.io.p_Z := p1p1top3.io.r_Z
            p3toCached.io.p_T := p1p1top3.io.r_T
            p3toCached.io.start := True
          }

          //          when(cntInit === 4) {
          //            rdStartParse := True
          //          }

          addInst1.io.A := p3toCached.io.add1_A
          addInst1.io.B := p3toCached.io.add1_B
          subInst1.io.A := p3toCached.io.sub1_A
          subInst1.io.B := p3toCached.io.sub1_B
          mulInst1.io.A := p3toCached.io.mul1_A
          mulInst1.io.B := p3toCached.io.mul1_B

          addInst1.io.start := p3toCached.io.fadd_start
          subInst1.io.start := p3toCached.io.fsub_start
          mulInst1.io.start := p3toCached.io.fmul_start

          oq_Z := p3toCached.io.r_Z

          rdStart := (io.mode === 1)? True | False
          cntInit := (io.mode === 1)? (cntInit + 1) | cntInit

          //          when(cntInit === 0){
          //            rdStart := True
          //          }

          //          rdStart := True

        }
        .whenIsActive {

          wrBit := 0
          rdStart := False

          addInst1.io.A := p3toCached.io.add1_A
          addInst1.io.B := p3toCached.io.add1_B
          subInst1.io.A := p3toCached.io.sub1_A
          subInst1.io.B := p3toCached.io.sub1_B
          mulInst1.io.A := p3toCached.io.mul1_A
          mulInst1.io.B := p3toCached.io.mul1_B

          addInst1.io.start := p3toCached.io.fadd_start
          subInst1.io.start := p3toCached.io.fsub_start
          mulInst1.io.start := p3toCached.io.fmul_start

          //          p3toCached.io.p_X := io.p_X
          //          p3toCached.io.p_Y := io.p_Y
          p3toCached.io.p_Z := op_Z
          //          p3toCached.io.p_T := io.p_T
          when(p3toCached.io.done === True) {
            wrStart := (io.mode === 0)? True | False
            oq_X := p3toCached.io.r_YplusX
            oq_Y := p3toCached.io.r_YminusX
            //            oq_Z := p3toCached.io.r_Z
            oq_T := p3toCached.io.r_T2d
            when(cntInit === U(7, 4 bits)) {
              goto(loopReadST)
            } elsewhen (cntInit(0).asBits === B(0, 1 bit)) {
              //              cntInit := cntInit + 1
              goto(p3DoubleST)
            } otherwise {
              //              cntInit := cntInit + 1
              goto(addCachedST)
            }
          }

        }

      p3DoubleST
        .onEntry {

          wrBit := 1

          cntInLoop := (loopMode === 1)? (cntInLoop + 1) | cntInLoop

          p3Double.io.p_X := (loopMode === 0)? int_X | p1p1top3.io.r_X
          p3Double.io.p_Y := (loopMode === 0)? int_Y | p1p1top3.io.r_Y
          p3Double.io.p_Z := (loopMode === 0)? int_Z | p1p1top3.io.r_Z
          p3Double.io.p_T := (loopMode === 0)? int_T | p1p1top3.io.r_T
          p3Double.io.start := True

          addInst1.io.A := p3Double.io.add1_A
          addInst1.io.B := p3Double.io.add1_B
          subInst1.io.A := p3Double.io.sub1_A
          subInst1.io.B := p3Double.io.sub1_B
          mulInst1.io.A := p3Double.io.mul1_A
          mulInst1.io.B := p3Double.io.mul1_B
          mulInst2.io.A := p3Double.io.mul2_A
          mulInst2.io.B := p3Double.io.mul2_B

          addInst1.io.start := p3Double.io.fadd1_start
          subInst1.io.start := p3Double.io.fsub1_start
          mulInst1.io.start := p3Double.io.fmul1_start
          mulInst2.io.start := p3Double.io.fmul2_start

          when(cntInit === 4) {
            rdStartParse := True
          }



        }
        .whenIsActive {
          wrBit := 1

          addInst1.io.A := p3Double.io.add1_A
          addInst1.io.B := p3Double.io.add1_B
          subInst1.io.A := p3Double.io.sub1_A
          subInst1.io.B := p3Double.io.sub1_B
          mulInst1.io.A := p3Double.io.mul1_A
          mulInst1.io.B := p3Double.io.mul1_B
          mulInst2.io.A := p3Double.io.mul2_A
          mulInst2.io.B := p3Double.io.mul2_B

          addInst1.io.start := p3Double.io.fadd1_start
          subInst1.io.start := p3Double.io.fsub1_start
          mulInst1.io.start := p3Double.io.fmul1_start
          mulInst2.io.start := p3Double.io.fmul2_start

          //          p3toCached.io.p_X :=  io.p_X
          //          p3toCached.io.p_Y := io.p_Y
          p3Double.io.p_Z := op_Z
          //          p3toCached.io.p_T := io.p_T
          when(p3Double.io.done === True) {
            oq_X := p3Double.io.r_X
            oq_Y := p3Double.io.r_Y
            oq_Z := p3Double.io.r_Z
            oq_T := p3Double.io.r_T
            goto(p1p1top3ST)
          }
        }
      p1p1top3ST
        .onEntry {
          when (loopMode === 0) {
            when(cntInit(0).asBits === 0) {
              p1p1top3.io.p_X := p3Double.io.r_X
              p1p1top3.io.p_Y := p3Double.io.r_Y
              p1p1top3.io.p_Z := p3Double.io.r_Z
              p1p1top3.io.p_T := p3Double.io.r_T
              p1p1top3.io.start := True
            } otherwise {
              p1p1top3.io.p_X := addCached.io.r_X
              p1p1top3.io.p_Y := addCached.io.r_Y
              p1p1top3.io.p_Z := addCached.io.r_Z
              p1p1top3.io.p_T := addCached.io.r_T
              p1p1top3.io.start := True
            }
          } otherwise {
            when(cntInLoop.asBits === 0) {
              p1p1top3.io.p_X := addCached.io.r_X
              p1p1top3.io.p_Y := addCached.io.r_Y
              p1p1top3.io.p_Z := addCached.io.r_Z
              p1p1top3.io.p_T := addCached.io.r_T
              p1p1top3.io.start := True
            } otherwise {
              p1p1top3.io.p_X := p3Double.io.r_X
              p1p1top3.io.p_Y := p3Double.io.r_Y
              p1p1top3.io.p_Z := p3Double.io.r_Z
              p1p1top3.io.p_T := p3Double.io.r_T
              p1p1top3.io.start := True
            }
            when(cntInLoop === U(4, 3 bits)){
              cntLoop := cntLoop - 1
            }

          }

          //          p1p1top3.io.p_X := oq_X
          //          p1p1top3.io.p_Y := oq_Y
          //          p1p1top3.io.p_Z := oq_Z
          //          p1p1top3.io.p_T := oq_T
          //          p1p1top3.io.start := True

          mulInst1.io.A := p1p1top3.io.mul1_A
          mulInst1.io.B := p1p1top3.io.mul1_B
          mulInst2.io.A := p1p1top3.io.mul2_A
          mulInst2.io.B := p1p1top3.io.mul2_B

          mulInst1.io.start := p1p1top3.io.fmul1_start
          mulInst2.io.start := p1p1top3.io.fmul2_start

          cntInit := (loopMode === 0)? (cntInit + 1) | U(0, 4 bits)

        }
        .whenIsActive {

          mulInst1.io.A := p1p1top3.io.mul1_A
          mulInst1.io.B := p1p1top3.io.mul1_B
          mulInst2.io.A := p1p1top3.io.mul2_A
          mulInst2.io.B := p1p1top3.io.mul2_B

          mulInst1.io.start := p1p1top3.io.fmul1_start
          mulInst2.io.start := p1p1top3.io.fmul2_start

          p1p1top3.io.p_X := oq_X
          p1p1top3.io.p_Y := oq_Y
          p1p1top3.io.p_Z := oq_Z
          p1p1top3.io.p_T := oq_T
          when(p1p1top3.io.done === True) {
            wrStart := (loopMode === 0)? ((cntInit < U(4, 4 bits))? True | False) | False
            oq_X := p1p1top3.io.r_X
            oq_Y := p1p1top3.io.r_Y
            oq_Z := p1p1top3.io.r_Z
            oq_T := p1p1top3.io.r_T
            when (loopMode === 0 && io.mode =/= 1){
              goto(p3ToCachedST)
            } elsewhen (loopMode === 0 && io.mode === 1) {
              allDone :=  True
              goto(doneST)
            } elsewhen(cntLoop === U(0, 6 bits) && flag === True) {
              allDone :=  True
              wrStart := True
              flag := False
              goto(doneST)
            } elsewhen(cntInLoop === U(4, 3 bits)) {
              flag := False
              goto(loopReadST)
            } otherwise {
              flag := False
              goto(p3DoubleST)
            }

          }
        }
      addCachedST
        .onEntry {
          wrBit := 1

          addCached.io.p_X := (io.mode === 1)? int_X | op_X
          addCached.io.p_Y := (io.mode === 1)? int_Y | op_Y
          addCached.io.p_Z := (io.mode === 1)? int_Z | op_Z
          addCached.io.p_T := (io.mode === 1)? int_T | op_T
          //          addCached.io.q_YplusX := oq_X
          //          addCached.io.q_YminusX := oq_Y
          //          addCached.io.q_Z := oq_Z
          //          addCached.io.q_T2D := oq_T
          addCached.io.q_YplusX := (loopMode === 0)? p3toCached.io.r_YplusX | tmp_X
          addCached.io.q_YminusX := (loopMode === 0)? p3toCached.io.r_YminusX | tmp_Y
          addCached.io.q_Z := (loopMode === 0)? oq_Z | tmp_Z
          addCached.io.q_T2D := (loopMode === 0)? p3toCached.io.r_T2d | tmp_T
          addCached.io.start := True

          addInst1.io.A := addCached.io.add1_A
          addInst1.io.B := addCached.io.add1_B
          subInst1.io.A := addCached.io.sub1_A
          subInst1.io.B := addCached.io.sub1_B
          mulInst1.io.A := addCached.io.mul1_A
          mulInst1.io.B := addCached.io.mul1_B
          addInst2.io.A := addCached.io.add2_A
          addInst2.io.B := addCached.io.add2_B
          subInst2.io.A := addCached.io.sub2_A
          subInst2.io.B := addCached.io.sub2_B
          mulInst2.io.A := addCached.io.mul2_A
          mulInst2.io.B := addCached.io.mul2_B

          addInst1.io.start := addCached.io.fadd1_start
          subInst1.io.start := addCached.io.fsub1_start
          addInst2.io.start := addCached.io.fadd2_start
          subInst2.io.start := addCached.io.fsub2_start
          mulInst1.io.start := addCached.io.fmul1_start
          mulInst2.io.start := addCached.io.fmul2_start

          rdStart := (loopMode === 0)? True | False
          flag := (loopMode === 1)? True | False
        }
        .whenIsActive {
          wrBit := 1
          rdStart := False

          addInst1.io.A := addCached.io.add1_A
          addInst1.io.B := addCached.io.add1_B
          subInst1.io.A := addCached.io.sub1_A
          subInst1.io.B := addCached.io.sub1_B
          mulInst1.io.A := addCached.io.mul1_A
          mulInst1.io.B := addCached.io.mul1_B
          addInst2.io.A := addCached.io.add2_A
          addInst2.io.B := addCached.io.add2_B
          subInst2.io.A := addCached.io.sub2_A
          subInst2.io.B := addCached.io.sub2_B
          mulInst2.io.A := addCached.io.mul2_A
          mulInst2.io.B := addCached.io.mul2_B

          addInst1.io.start := addCached.io.fadd1_start
          subInst1.io.start := addCached.io.fsub1_start
          addInst2.io.start := addCached.io.fadd2_start
          subInst2.io.start := addCached.io.fsub2_start
          mulInst1.io.start := addCached.io.fmul1_start
          mulInst2.io.start := addCached.io.fmul2_start

          //          p3toCached.io.p_X := io.p_X
          //          p3toCached.io.p_Y := io.p_Y
          addCached.io.q_YplusX := oq_X
          addCached.io.q_YminusX := oq_Y
          //          p3toCached.io.p_T := io.p_T
          when(addCached.io.done === True) {
            oq_X := addCached.io.r_X
            oq_Y := addCached.io.r_Y
            oq_Z := addCached.io.r_Z
            oq_T := addCached.io.r_T
            goto(p1p1top3ST)
          }
        }
      negT2DST
        .onEntry{
          negT2D.io.T2D := io.memP_Y

          negT2D.io.start := True

          subInst1.io.A := negT2D.io.sub1_A
          subInst1.io.B := negT2D.io.sub1_B
          subInst1.io.start := negT2D.io.fsub_start
          cntInLoop := 0
        }
        .whenIsActive{

          when(negT2D.io.done === True){
            tmp_X := (parseScalar.io.e(cntLoop)(4 downto 4) === 0)? int_X | int_Y
            tmp_Y := (parseScalar.io.e(cntLoop)(4 downto 4) === 0)? int_Y | int_X
            tmp_Z := int_Z
            tmp_T := (parseScalar.io.e(cntLoop)(4 downto 4) === 0)? int_T | negT2D.io.neg_T2D

            oq_X := tmp_X
            oq_Y := tmp_Y
            oq_Z := tmp_Z
            oq_T := tmp_T
            goto(addCachedST)
          }

        }


      loopReadST
        .onEntry{
          when(cntLoop === 63){
            op_X := B(0, 256 bits)
            op_Y := B(1, 256 bits)
            op_Z := B(1, 256 bits)
            op_T := B(0, 256 bits)
            loopMode := B(1, 2 bits)
          } otherwise{
            op_X := p1p1top3.io.r_X
            op_Y := p1p1top3.io.r_Y
            op_Z := p1p1top3.io.r_Z
            op_T := p1p1top3.io.r_T
          }
          //          cntInit := U(7, 4 bits)

          //          wrBit := 1

        }
        .whenIsActive{
          wrBit := 1
          rdStart := True
          when(cntRdRetrieve === 2) {
            goto(negT2DST)
          }
        }
      doneST
        .whenIsActive{
          when(io.ready){
            goto(initST)
          }
        }
    }

  }

  parseScalar.io.start := coreArea.parseStart
  parseScalar.io.a := coreArea.scalar_A
  parseScalar.clk := clk
  parseScalar.rst := rst

}


object ScalarMultGen{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    config.generateVerilog({
      val toplevel = new scalar_mult()
      toplevel
    })
  }
}

//1: init
//2: retrieve
//3: 512
//4: p3tocached
//5: p1p1_to_p3
//6: double
//7: addcached
//8: loopread
//9: negt2d