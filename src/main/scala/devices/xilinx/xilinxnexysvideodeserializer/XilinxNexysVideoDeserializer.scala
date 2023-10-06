package devices.xilinx.xilinxnexysvideodeserializer

import chisel3._
import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config.Parameters
import sifive.fpgashells.clocks.{PLLInClockParameters, PLLOutClockParameters, PLLParameters}
import sifive.fpgashells.ip.xilinx.{DiffSeries7MMCM, IBUFDS, SelectIO, Series7MMCM}

case class XilinxNexysVideoDeserializerParams(
  channels : Int,
  pll: PLLParameters = PLLParameters(
    name = "lvds_pll",
    input = PLLInClockParameters(freqMHz = 450.0),
    req = Seq(
      PLLOutClockParameters(freqMHz = 450.0),
      PLLOutClockParameters(freqMHz = 112.5)
    )
  )
)

class NexysVideoDeserializerIO(val channels: Int) extends Bundle {
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
  val o_frame: UInt = Output(UInt(8.W))
  val o_valid: UInt = Output(UInt(8.W))
  val o_data: Vec[UInt] = Output(Vec(channels, UInt(8.W)))
  // Reset
  val i_rst: Bool = Input(Bool())
}

class XilinxNexysVideoDeserializer(c: XilinxNexysVideoDeserializerParams)(implicit p: Parameters) extends LazyModule {
  
  lazy val module = new Impl
  class Impl extends LazyRawModuleImp(this) {
    val io: NexysVideoDeserializerIO = IO(new NexysVideoDeserializerIO(c.channels))

//    // Differential buffer
//    val ibufds: IBUFDS = Module(new IBUFDS)
//    ibufds.suggestName(s"${name}_ibufds")
    // PLL
    val pll: DiffSeries7MMCM = Module(new DiffSeries7MMCM(c.pll))

//    // Connect Buffer and PLL
//    ibufds.io.I := io.i_clk_p
//    ibufds.io.IB := io.i_clk_n

    pll.io.clk_in1_p := io.i_clk_p //ibufds.io.O
    pll.io.clk_in1_n := io.i_clk_n //ibufds.io.O
    pll.io.reset := io.i_rst
    
    childClock := pll.io.clk_out2.get
    childReset := io.i_rst && !pll.io.locked

    // 7 series SelectIO
    private val selectio_frame = Module(new SelectIO)
    private val selectio_valid = Module(new SelectIO)
    private val selectio_data = Seq.fill(c.channels) {
      Module(new SelectIO)
    }

    // Connect modules
    selectio_frame.io.clk_in := pll.io.clk_out1.get
    selectio_frame.io.clk_div_in := pll.io.clk_out2.get
    selectio_frame.io.io_reset := childReset
    selectio_frame.io.bitslip := 0.U
    selectio_frame.io.data_in_from_pins_p := io.i_frame_p
    selectio_frame.io.data_in_from_pins_n := io.i_frame_n
    io.o_frame := selectio_frame.io.data_in_to_device

    selectio_valid.io.clk_in := pll.io.clk_out1.get
    selectio_valid.io.clk_div_in := pll.io.clk_out2.get
    selectio_valid.io.io_reset := childReset
    selectio_valid.io.bitslip := 0.U
    selectio_valid.io.data_in_from_pins_p := io.i_valid_p
    selectio_valid.io.data_in_from_pins_n := io.i_valid_n
    io.o_valid := selectio_valid.io.data_in_to_device
    
    selectio_data.zipWithIndex.foreach { case (m, i) =>
      m.io.clk_in := pll.io.clk_out1.get
      m.io.clk_div_in := pll.io.clk_out2.get
      m.io.io_reset := childReset
      m.io.bitslip := 0.U
      m.io.data_in_from_pins_p := io.i_data_p(i)
      m.io.data_in_from_pins_n := io.i_data_n(i)
      io.o_data(i) := m.io.data_in_to_device
    }
  }
}

