package devices.xilinx.xilinxnexysvideodeserializer

import chisel3._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util.ResetCatchAndSync
import org.chipsalliance.cde.config.Parameters
import sifive.fpgashells.clocks.{PLLInClockParameters, PLLOutClockParameters, PLLParameters}
import sifive.fpgashells.ip.xilinx.{DiffSeries7MMCM, DiffSeries7PLL, RESETSYS, SelectIO}

case class XilinxNexysVideoDeserializerParams(
  channels : Int,
  chips    : Int,
  pll: PLLParameters = PLLParameters(
    name = "deser_pll",
    input = PLLInClockParameters(freqMHz = 300.0),
    req = Seq(
      PLLOutClockParameters(freqMHz = 300.0),
      PLLOutClockParameters(freqMHz = 75.0)
    )
  )
)

class LVDSBundle(val channels:Int) extends Bundle {
  // LVDS clock, data, frame and valid
  val i_clk_p: Clock = Input(Clock())
  val i_clk_n: Clock = Input(Clock())
  val i_data_p: Vec[Bool] = Input(Vec(channels, Bool()))
  val i_data_n: Vec[Bool] = Input(Vec(channels, Bool()))
  val i_valid_p: Bool = Input(Bool())
  val i_valid_n: Bool = Input(Bool())
  val i_frame_p: Bool = Input(Bool())
  val i_frame_n: Bool = Input(Bool())
  // Outputs
  val o_clock: Clock = Output(Clock())
  val o_reset: Reset = Output(Bool())
  val o_frame: UInt = Output(UInt(8.W))
  val o_valid: UInt = Output(UInt(8.W))
  val o_data: Vec[UInt] = Output(Vec(channels, UInt(8.W)))
  // Reset
  val i_rst: Bool = Input(Bool())
}
class NexysVideoDeserializerIO(val channels: Int, val chips: Int) extends Bundle {
  val lvds: Vec[LVDSBundle] = Vec(chips, new LVDSBundle(channels))
}

class XilinxNexysVideoDeserializer(c: XilinxNexysVideoDeserializerParams)(implicit p: Parameters) extends LazyModule {

  lazy val module = new Impl
  class Impl extends LazyRawModuleImp(this) {
    val io: NexysVideoDeserializerIO = IO(new NexysVideoDeserializerIO(c.channels, c.chips))

    // PLL
    val pll: Seq[DiffSeries7MMCM] = Seq.fill(c.chips){Module(new DiffSeries7MMCM(c.pll))}

    pll.zip(io.lvds).foreach{case (pll, io) =>
      pll.io.clk_in1_p := io.i_clk_p
      pll.io.clk_in1_n := io.i_clk_n
      pll.io.reset := io.i_rst

//      val resetSync = ResetCatchAndSync(pll.io.clk_out2.get, io.i_rst && !pll.io.locked)
      val rst_deser  = Module(new RESETSYS)
      rst_deser.io.slowest_sync_clk := pll.io.clk_out2.get
      rst_deser.io.ext_reset_in := io.i_rst
      rst_deser.io.aux_reset_in := 0.U
      rst_deser.io.mb_debug_sys_rst := 0.U
      rst_deser.io.dcm_locked := pll.io.locked
      rst_deser.io.bus_struct_reset := DontCare
      rst_deser.io.interconnect_aresetn := DontCare
      rst_deser.io.peripheral_aresetn := DontCare
      io.o_clock := pll.io.clk_out2.get
      io.o_reset := rst_deser.io.mb_reset

      // 7 series SelectIO
      val selectio_frame = Module(new SelectIO)
      val selectio_valid = Module(new SelectIO)
      val selectio_data = Seq.fill(c.channels) {
        Module(new SelectIO)
      }

      // Connect modules
      selectio_frame.io.clk_in := pll.io.clk_out1.get
      selectio_frame.io.clk_div_in := pll.io.clk_out2.get
      selectio_frame.io.io_reset := rst_deser.io.peripheral_reset
      selectio_frame.io.bitslip := 0.U
      selectio_frame.io.data_in_from_pins_p := io.i_frame_p
      selectio_frame.io.data_in_from_pins_n := io.i_frame_n
      io.o_frame := selectio_frame.io.data_in_to_device

      selectio_valid.io.clk_in := pll.io.clk_out1.get
      selectio_valid.io.clk_div_in := pll.io.clk_out2.get
      selectio_valid.io.io_reset := rst_deser.io.peripheral_reset
      selectio_valid.io.bitslip := 0.U
      selectio_valid.io.data_in_from_pins_p := io.i_valid_p
      selectio_valid.io.data_in_from_pins_n := io.i_valid_n
      io.o_valid := selectio_valid.io.data_in_to_device

      selectio_data.zipWithIndex.foreach { case (m, i) =>
        m.io.clk_in := pll.io.clk_out1.get
        m.io.clk_div_in := pll.io.clk_out2.get
        m.io.io_reset := rst_deser.io.peripheral_reset
        m.io.bitslip := 0.U
        m.io.data_in_from_pins_p := io.i_data_p(i)
        m.io.data_in_from_pins_n := io.i_data_n(i)
        io.o_data(i) := m.io.data_in_to_device
      }
    }
  }
}

