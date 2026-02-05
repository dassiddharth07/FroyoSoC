package dma_unit

import scala.util.Random

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.amba4.axilite._
import spinal.lib.fsm._

import dma_unit.axi._
import spinal.lib.eda.altera._

object Axi4CoreConfig{
  def getAxi4Config = Axi4Config(
    addressWidth = 32,
    dataWidth    = 32,
    useId = false,
    useRegion = false,
    useLock = false,
    useQos = false,
    useSize = false
  )
}


class DmaUnit extends Component {
  val io = new Bundle {
    val irq =  out Bool()
    val fireJob = in Bool()
    val jobDone = out Bool()
    val readAddr = in UInt(32 bits)
    val writeAddr = in UInt(32 bits)
    val wordCount = in UInt(24 bits)
    val axi_master = master(Axi4(Axi4CoreConfig.getAxi4Config))
  }
  val axiReader = AxiReader(Axi4CoreConfig.getAxi4Config, 24, 32, 2)
  val axiWriter = AxiWriter(Axi4CoreConfig.getAxi4Config, 24, 32)
  val read_job = axiReader.readJobFactory()
  val read_job_valid = Reg(Bool) init(False)
  val write_job = axiWriter.writeJobFactory()
  val write_job_valid = Reg(Bool) init(False)
  val irq = Reg(Bool) init(False)
  val fire = Bool
  
  io.irq := irq
  read_job_valid := False
  write_job_valid := False
  fire := io.fireJob
  read_job.address := io.readAddr
  write_job.address := io.writeAddr
  read_job.word_count := io.wordCount
  write_job.word_count := read_job.word_count
  
  io.axi_master << axiReader.io.axi_master
  io.axi_master << axiWriter.io.axi_master
  
  axiReader.io.read_job.valid := read_job_valid
  axiReader.io.read_job.payload := read_job
  axiWriter.io.write_job.valid := write_job_valid
  axiWriter.io.write_job.payload := write_job
  axiWriter.io.write_in << axiReader.io.read_out

  val fsm = new StateMachine {

    irq := False
    io.jobDone := False

    val idle : State = new State with EntryPoint {
      whenIsActive(
        when(fire) {
          read_job_valid := True
          write_job_valid := True
          goto(waitForWrite)
        }
      )
    }

    val waitForWrite : State = new State {
      whenIsActive(
        when(axiWriter.io.busy) {
          goto(process)
        }
      )
    }

    val process : State = new State {
      whenIsActive {
        when(!axiReader.io.busy && !axiWriter.io.busy) {
          io.jobDone := True
        }
        when(!axiReader.io.busy && !axiWriter.io.busy && !fire) {
          goto(idle)
        }
      }
      onExit(
        irq := True
      )
    }
  }
}

object DMaUnitGen{
  def main(args: Array[String]) {
    val config = SpinalConfig(targetDirectory = "verilog_outputs")
    config.generateVerilog({
      val toplevel = new DmaUnit()
      toplevel
    })
  }
}