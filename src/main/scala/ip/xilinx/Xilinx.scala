package sifive.fpgashells.ip.xilinx

import chisel3._
import chisel3.experimental.Analog
import chisel3.util.HasBlackBoxInline
import freechips.rocketchip.util.ElaborationArtefacts
import sifive.fpgashells.clocks._

//========================================================================
// This file contains common devices used by our Xilinx FPGA flows and some
// BlackBox modules used in the Xilinx FPGA flows
//========================================================================

//-------------------------------------------------------------------------
// mmcm
//-------------------------------------------------------------------------
/** mmcm: This is generated by the Xilinx IP Generation Scripts */

class mmcm extends BlackBox {
  val io = IO(new Bundle {
    val clk_in1  = Input(Clock())
    val clk_out1 = Output(Clock())
    val clk_out2 = Output(Clock())
    val clk_out3 = Output(Clock())
    val resetn   = Input(Bool())
    val locked   = Output(Bool())
  })
}

//-------------------------------------------------------------------------
// reset_sys
//-------------------------------------------------------------------------
/** reset_sys: This is generated by the Xilinx IP Generation Scripts */

class reset_sys extends BlackBox {
  val io = IO(new Bundle {
    val slowest_sync_clk     = Input(Clock())
    val ext_reset_in         = Input(Bool())
    val aux_reset_in         = Input(Bool())
    val mb_debug_sys_rst     = Input(Bool())
    val dcm_locked           = Input(Bool())
    val mb_reset             = Output(Bool())
    val bus_struct_reset     = Output(Bool())
    val peripheral_reset     = Output(Bool())
    val interconnect_aresetn = Output(Bool())
    val peripheral_aresetn   = Output(Bool())
  })
}

//-------------------------------------------------------------------------
// reset_mig
//-------------------------------------------------------------------------
/** reset_mig: This is generated by the Xilinx IP Generation Scripts */

class reset_mig extends BlackBox {
  val io = IO(new Bundle {
    val slowest_sync_clk     = Input(Clock())
    val ext_reset_in         = Input(Bool())
    val aux_reset_in         = Input(Bool())
    val mb_debug_sys_rst     = Input(Bool())
    val dcm_locked           = Input(Bool())
    val mb_reset             = Output(Bool())
    val bus_struct_reset     = Output(Bool())
    val peripheral_reset     = Output(Bool())
    val interconnect_aresetn = Output(Bool())
    val peripheral_aresetn   = Output(Bool())
  })
}

//-------------------------------------------------------------------------
// PowerOnResetFPGAOnly
//-------------------------------------------------------------------------
/** PowerOnResetFPGAOnly -- this generates a power_on_reset signal using
  * initial blocks.  It is synthesizable on FPGA flows only.
  */

// This is a FPGA-Only construct, which uses
// 'initial' constructions
class PowerOnResetFPGAOnly extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val power_on_reset = Output(Bool())
  })

  setInline(s"PowerOnResetFPGAOnly.v",
    s"""(* keep_hierarchy = "yes" *)
       |module PowerOnResetFPGAOnly(
       |  input wire clock,
       |  (* dont_touch = "true" *) output reg power_on_reset
       |);
       |  initial begin
       |    power_on_reset <= 1'b1;
       |  end
       |  always @(posedge clock) begin
       |    power_on_reset <= 1'b0;
       |  end
       |endmodule
       |""".stripMargin)
}

object PowerOnResetFPGAOnly {
  def apply (clk: Clock, name: String): Bool = {
    val por = Module(new PowerOnResetFPGAOnly())
    por.suggestName(name)
    por.io.clock := clk
    por.io.power_on_reset
  }
  def apply (clk: Clock): Bool = apply(clk, "fpga_power_on")
}


//-------------------------------------------------------------------------
// vc707_sys_clock_mmcm
//-------------------------------------------------------------------------
//IP : xilinx mmcm with "NO_BUFFER" input clock
class Series7MMCM(c : PLLParameters) extends BlackBox with PLLInstance {
  val io = IO(new Bundle {
    val clk_in1   = Input(Clock())
    val clk_out1  = if (c.req.size >= 1) Some(Output(Clock())) else None
    val clk_out2  = if (c.req.size >= 2) Some(Output(Clock())) else None
    val clk_out3  = if (c.req.size >= 3) Some(Output(Clock())) else None
    val clk_out4  = if (c.req.size >= 4) Some(Output(Clock())) else None
    val clk_out5  = if (c.req.size >= 5) Some(Output(Clock())) else None
    val clk_out6  = if (c.req.size >= 6) Some(Output(Clock())) else None
    val clk_out7  = if (c.req.size >= 7) Some(Output(Clock())) else None
    val reset     = Input(Bool())
    val locked    = Output(Bool())
  })

  val moduleName = c.name
  override def desiredName = c.name

  def getClocks = Seq() ++ io.clk_out1 ++ io.clk_out2 ++ 
                           io.clk_out3 ++ io.clk_out4 ++ 
                           io.clk_out5 ++ io.clk_out6 ++ 
                           io.clk_out7
  def getInput = io.clk_in1
  def getReset = Some(io.reset)
  def getLocked = io.locked
  def getClockNames = Seq.tabulate (c.req.size) { i =>
    s"${c.name}/inst/mmcm_adv_inst/CLKOUT${i}"
  }

  val used = Seq.tabulate(7) { i =>
    s" CONFIG.CLKOUT${i+1}_USED {${i < c.req.size}} \\\n"
  }.mkString

  val outputs = c.req.zipWithIndex.map { case (r, i) =>
    s""" CONFIG.CLKOUT${i+1}_REQUESTED_OUT_FREQ {${r.freqMHz}} \\
       | CONFIG.CLKOUT${i+1}_REQUESTED_PHASE {${r.phaseDeg}} \\
       | CONFIG.CLKOUT${i+1}_REQUESTED_DUTY_CYCLE {${r.dutyCycle}} \\
       |""".stripMargin
  }.mkString

  val checks = c.req.zipWithIndex.map { case (r, i) =>
    val f = if (i == 0) "_F" else ""
    val phaseMin = r.phaseDeg - r.phaseErrorDeg
    val phaseMax = r.phaseDeg + r.phaseErrorDeg
    val freqMin = r.freqMHz * (1 - r.freqErrorPPM / 1000000)
    val freqMax = r.freqMHz * (1 + r.freqErrorPPM / 1000000)
    s"""set jitter [get_property CONFIG.CLKOUT${i+1}_JITTER [get_ips ${moduleName}]]
       |if {$$jitter > ${r.jitterPS}} {
       |  puts "Output jitter $$jitter ps exceeds required limit of ${r.jitterPS}"
       |  exit 1
       |}
       |set phase [get_property CONFIG.MMCM_CLKOUT${i}_PHASE [get_ips ${moduleName}]]
       |if {$$phase < ${phaseMin} || $$phase > ${phaseMax}} {
       |  puts "Achieved phase $$phase degrees is outside tolerated range ${phaseMin}-${phaseMax}"
       |  exit 1
       |}
       |set div2 [get_property CONFIG.MMCM_CLKOUT${i}_DIVIDE${f} [get_ips ${moduleName}]]
       |set freq [expr { ${c.input.freqMHz} * $$mult / $$div1 / $$div2 }]
       |if {$$freq < ${freqMin} || $$freq > ${freqMax}} {
       |  puts "Achieved frequency $$freq MHz is outside tolerated range ${freqMin}-${freqMax}"
       |  exit 1
       |}
       |puts "Achieve frequency $$freq MHz phase $$phase degrees jitter $$jitter ps"
       |""".stripMargin
  }.mkString


  val aligned = if (c.input.feedback) " CONFIG.USE_PHASE_ALIGNMENT {true} \\\n" else ""

  ElaborationArtefacts.add(s"${moduleName}.vivado.tcl",
    s"""create_ip -name clk_wiz -vendor xilinx.com -library ip -module_name \\
       | ${moduleName} -dir $$ipdir -force
       |set_property -dict [list \\
       | CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \\
       | CONFIG.PRIM_SOURCE {No_buffer} \\
       | CONFIG.NUM_OUT_CLKS {${c.req.size.toString}} \\
       | CONFIG.PRIM_IN_FREQ {${c.input.freqMHz.toString}} \\
       | CONFIG.CLKIN1_JITTER_PS {${c.input.jitter}} \\
       |${used}${aligned}${outputs}] [get_ips ${moduleName}]
       |set mult [get_property CONFIG.MMCM_CLKFBOUT_MULT_F [get_ips ${moduleName}]]
       |set div1 [get_property CONFIG.MMCM_DIVCLK_DIVIDE [get_ips ${moduleName}]]
       |${checks}""".stripMargin)
}

//-------------------------------------------------------------------------
// vc707reset
//-------------------------------------------------------------------------

class vc707reset() extends BlackBox
{
  val io = IO(new Bundle{
    val areset = Input(Bool())
    val clock1 = Input(Clock())
    val reset1 = Output(Bool())
    val clock2 = Input(Clock())
    val reset2 = Output(Bool())
    val clock3 = Input(Clock())
    val reset3 = Output(Bool())
    val clock4 = Input(Clock())
    val reset4 = Output(Bool())
  })
}

//-------------------------------------------------------------------------
// vcu118_sys_clock_mmcm
//-------------------------------------------------------------------------
//IP : xilinx mmcm with "NO_BUFFER" input clock

class vcu118_sys_clock_mmcm0 extends BlackBox {
  val io = IO(new Bundle {
    val clk_in1   = Input(Bool())
    val clk_out1  = Output(Clock())
    val clk_out2  = Output(Clock())
    val clk_out3  = Output(Clock())
    val clk_out4  = Output(Clock())
    val clk_out5  = Output(Clock())
    val clk_out6  = Output(Clock())
    val clk_out7  = Output(Clock())
    val reset     = Input(Bool())
    val locked    = Output(Bool())
  })

  ElaborationArtefacts.add(
    "vcu118_sys_clock_mmcm0.vivado.tcl",
    """create_ip -name clk_wiz -vendor xilinx.com -library ip -module_name vcu118_sys_clock_mmcm0 -dir $ipdir -force
    set_property -dict [list \
    CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \
    CONFIG.PRIM_SOURCE {No_buffer} \
    CONFIG.CLKOUT2_USED {true} \
    CONFIG.CLKOUT3_USED {true} \
    CONFIG.CLKOUT4_USED {true} \
    CONFIG.CLKOUT5_USED {true} \
    CONFIG.CLKOUT6_USED {true} \
    CONFIG.CLKOUT7_USED {true} \
    CONFIG.CLKOUT1_REQUESTED_OUT_FREQ {12.5} \
    CONFIG.CLKOUT2_REQUESTED_OUT_FREQ {25} \
    CONFIG.CLKOUT3_REQUESTED_OUT_FREQ {37.5} \
    CONFIG.CLKOUT4_REQUESTED_OUT_FREQ {50} \
    CONFIG.CLKOUT5_REQUESTED_OUT_FREQ {100} \
    CONFIG.CLKOUT6_REQUESTED_OUT_FREQ {150.000} \
    CONFIG.CLKOUT7_REQUESTED_OUT_FREQ {75} \
    CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \
    CONFIG.PRIM_IN_FREQ {250.000} \
    CONFIG.CLKIN1_JITTER_PS {50.0} \
    CONFIG.MMCM_DIVCLK_DIVIDE {5} \
    CONFIG.MMCM_CLKFBOUT_MULT_F {24.000} \
    CONFIG.MMCM_CLKIN1_PERIOD {4.000} \
    CONFIG.MMCM_CLKOUT0_DIVIDE_F {96.000} \
    CONFIG.MMCM_CLKOUT1_DIVIDE {48} \
    CONFIG.MMCM_CLKOUT2_DIVIDE {32} \
    CONFIG.MMCM_CLKOUT3_DIVIDE {24} \
    CONFIG.MMCM_CLKOUT4_DIVIDE {12} \
    CONFIG.MMCM_CLKOUT5_DIVIDE {8} \
    CONFIG.MMCM_CLKOUT6_DIVIDE {16} \
    CONFIG.NUM_OUT_CLKS {7} \
    CONFIG.CLKOUT1_JITTER {213.008} \
    CONFIG.CLKOUT1_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT2_JITTER {179.547} \
    CONFIG.CLKOUT2_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT3_JITTER {164.187} \
    CONFIG.CLKOUT3_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT4_JITTER {154.688} \
    CONFIG.CLKOUT4_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT5_JITTER {135.165} \
    CONFIG.CLKOUT5_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT6_JITTER {126.046} \
    CONFIG.CLKOUT6_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT7_JITTER {142.781} \
    CONFIG.CLKOUT7_PHASE_ERROR {154.678}] [get_ips vcu118_sys_clock_mmcm0] """
  )
}

class vcu118_sys_clock_mmcm1 extends BlackBox {
  val io = IO(new Bundle {
    val clk_in1   = Input(Bool())
    val clk_out1  = Output(Clock())
    val clk_out2  = Output(Clock())
    val reset     = Input(Bool())
    val locked    = Output(Bool())
  })

  ElaborationArtefacts.add(
    "vcu118_sys_clock_mmcm1.vivado.tcl",
    """create_ip -name clk_wiz -vendor xilinx.com -library ip -module_name vcu118_sys_clock_mmcm1 -dir $ipdir -force
    set_property -dict [list \
    CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \
    CONFIG.PRIM_SOURCE {No_buffer} \
    CONFIG.CLKOUT2_USED {true} \
    CONFIG.CLKOUT3_USED {false} \
    CONFIG.CLKOUT4_USED {false} \
    CONFIG.CLKOUT5_USED {false} \
    CONFIG.CLKOUT6_USED {false} \
    CONFIG.CLKOUT7_USED {false} \
    CONFIG.CLKOUT1_REQUESTED_OUT_FREQ {32.5} \
    CONFIG.CLKOUT2_REQUESTED_OUT_FREQ {65} \
    CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \
    CONFIG.PRIM_IN_FREQ {250.000} \
    CONFIG.CLKIN1_JITTER_PS {50.0} \
    CONFIG.MMCM_DIVCLK_DIVIDE {25} \
    CONFIG.MMCM_CLKFBOUT_MULT_F {117.000} \
    CONFIG.MMCM_CLKIN1_PERIOD {4.000} \
    CONFIG.MMCM_CLKOUT0_DIVIDE_F {36.000} \
    CONFIG.MMCM_CLKOUT1_DIVIDE {18} \
    CONFIG.MMCM_CLKOUT2_DIVIDE {1} \
    CONFIG.MMCM_CLKOUT3_DIVIDE {1} \
    CONFIG.MMCM_CLKOUT4_DIVIDE {1} \
    CONFIG.MMCM_CLKOUT5_DIVIDE {1} \
    CONFIG.MMCM_CLKOUT6_DIVIDE {1} \
    CONFIG.NUM_OUT_CLKS {2} \
    CONFIG.CLKOUT1_JITTER {257.594} \
    CONFIG.CLKOUT1_PHASE_ERROR {366.693} \
    CONFIG.CLKOUT2_JITTER {232.023} \
    CONFIG.CLKOUT2_PHASE_ERROR {366.693}] \
    [get_ips vcu118_sys_clock_mmcm1] """
  )
}

//-------------------------------------------------------------------------
// vcu118reset
//-------------------------------------------------------------------------

class vcu118reset() extends BlackBox
{
  val io = IO(new Bundle{
    val areset = Input(Bool())
    val clock1 = Input(Clock())
    val reset1 = Output(Bool())
    val clock2 = Input(Clock())
    val reset2 = Output(Bool())
    val clock3 = Input(Clock())
    val reset3 = Output(Bool())
    val clock4 = Input(Clock())
    val reset4 = Output(Bool())
  })
}

//-------------------------------------------------------------------------
// sdio_spi_bridge
//-------------------------------------------------------------------------

class sdio_spi_bridge() extends BlackBox
{
  val io = IO(new Bundle{
    val clk      = Input(Clock())
    val reset    = Input(Bool())
    val sd_cmd   = Analog(1.W)
    val sd_dat   = Analog(4.W)
    val spi_sck  = Input(Bool())
    val spi_cs   = Input(Bool())
    val spi_dq_o = Input(Bits(4.W))
    val spi_dq_i = Output(Bits(4.W))
  })
}

// SelectIO 7 Series
class SelectIO extends BlackBox {
  val io = IO(new Bundle {
    val clk_in     = Input(Clock())
    val clk_div_in = Input(Clock())
    val io_reset   = Input(Bool())
    val bitslip    = Input(UInt(1.W))
    val data_in_from_pins_p = Input(Bool())
    val data_in_from_pins_n = Input(Bool())
    val data_in_to_device   = Output(UInt(8.W))
  })

  ElaborationArtefacts.add(
    "selectio.vivado.tcl",
    """create_ip -name selectio_wiz -vendor xilinx.com -library ip -version 5.1 -module_name SelectIO
    set_property -dict [list \
    CONFIG.BUS_SIG_TYPE {DIFF} \
    CONFIG.BUS_IO_STD {LVDS_25} \
    CONFIG.SELIO_ACTIVE_EDGE {DDR} \
    CONFIG.USE_SERIALIZATION {true} \
    CONFIG.SERIALIZATION_FACTOR {8} \
    CONFIG.SELIO_CLK_BUF {MMCM} \
    CONFIG.SYSTEM_DATA_WIDTH {1} \
    CONFIG.SELIO_INTERFACE_TYPE {NETWORKING} \
    CONFIG.SELIO_CLK_IO_STD {HSTL_I} \
    CONFIG.CLK_FWD_SIG_TYPE {DIFF} \
    CONFIG.CLK_FWD_IO_STD {LVDS_25}] [get_ips SelectIO] """
  )
}

// SelectIO 7 Series
class ILA_DATARX extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val probe0 = Input(UInt(16.W))
    val probe1 = Input(UInt(16.W))
    val probe2 = Input(UInt(16.W))
    val probe3 = Input(UInt(16.W))
    val probe4 = Input(UInt(1.W))
    val probe5 = Input(UInt(1.W))
  })

  ElaborationArtefacts.add(
    "ila.vivado.tcl",
    """create_ip -name ila -vendor xilinx.com -library ip -version 6.2 -module_name ILA_DATARX
    set_property -dict [list \
    CONFIG.C_PROBE5_WIDTH {1} \
    CONFIG.C_PROBE4_WIDTH {1} \
    CONFIG.C_PROBE3_WIDTH {16} \
    CONFIG.C_PROBE2_WIDTH {16} \
    CONFIG.C_PROBE1_WIDTH {16} \
    CONFIG.C_PROBE0_WIDTH {16} \
    CONFIG.C_DATA_DEPTH {1024} \
    CONFIG.C_NUM_OF_PROBES {6}] [get_ips ILA_DATARX] """
  )
}

//IP : xilinx mmcm with differential input clock
class DiffSeries7MMCM(c : PLLParameters) extends BlackBox with PLLInstance {
  val io = IO(new Bundle {
    val clk_in1_p   = Input(Clock())
    val clk_in1_n   = Input(Clock())
    val clk_out1  = if (c.req.size >= 1) Some(Output(Clock())) else None
    val clk_out2  = if (c.req.size >= 2) Some(Output(Clock())) else None
    val clk_out3  = if (c.req.size >= 3) Some(Output(Clock())) else None
    val clk_out4  = if (c.req.size >= 4) Some(Output(Clock())) else None
    val clk_out5  = if (c.req.size >= 5) Some(Output(Clock())) else None
    val clk_out6  = if (c.req.size >= 6) Some(Output(Clock())) else None
    val clk_out7  = if (c.req.size >= 7) Some(Output(Clock())) else None
    val reset     = Input(Bool())
    val locked    = Output(Bool())
  })

  val moduleName = c.name
  override def desiredName = c.name

  def getClocks = Seq() ++ io.clk_out1 ++ io.clk_out2 ++
    io.clk_out3 ++ io.clk_out4 ++
    io.clk_out5 ++ io.clk_out6 ++
    io.clk_out7
  def getInput = io.clk_in1_p
  def getReset = Some(io.reset)
  def getLocked = io.locked
  def getClockNames = Seq.tabulate (c.req.size) { i =>
    s"${c.name}/inst/mmcm_adv_inst/CLKOUT${i}"
  }

  val used = Seq.tabulate(7) { i =>
    s" CONFIG.CLKOUT${i+1}_USED {${i < c.req.size}} \\\n"
  }.mkString

  val outputs = c.req.zipWithIndex.map { case (r, i) =>
    s""" CONFIG.CLKOUT${i+1}_REQUESTED_OUT_FREQ {${r.freqMHz}} \\
       | CONFIG.CLKOUT${i+1}_REQUESTED_PHASE {${r.phaseDeg}} \\
       | CONFIG.CLKOUT${i+1}_REQUESTED_DUTY_CYCLE {${r.dutyCycle}} \\
       |""".stripMargin
  }.mkString

  val checks = c.req.zipWithIndex.map { case (r, i) =>
    val f = if (i == 0) "_F" else ""
    val phaseMin = r.phaseDeg - r.phaseErrorDeg
    val phaseMax = r.phaseDeg + r.phaseErrorDeg
    val freqMin = r.freqMHz * (1 - r.freqErrorPPM / 1000000)
    val freqMax = r.freqMHz * (1 + r.freqErrorPPM / 1000000)
    s"""set jitter [get_property CONFIG.CLKOUT${i+1}_JITTER [get_ips ${moduleName}]]
       |if {$$jitter > ${r.jitterPS}} {
       |  puts "Output jitter $$jitter ps exceeds required limit of ${r.jitterPS}"
       |  exit 1
       |}
       |set phase [get_property CONFIG.MMCM_CLKOUT${i}_PHASE [get_ips ${moduleName}]]
       |if {$$phase < ${phaseMin} || $$phase > ${phaseMax}} {
       |  puts "Achieved phase $$phase degrees is outside tolerated range ${phaseMin}-${phaseMax}"
       |  exit 1
       |}
       |set div2 [get_property CONFIG.MMCM_CLKOUT${i}_DIVIDE${f} [get_ips ${moduleName}]]
       |set freq [expr { ${c.input.freqMHz} * $$mult / $$div1 / $$div2 }]
       |if {$$freq < ${freqMin} || $$freq > ${freqMax}} {
       |  puts "Achieved frequency $$freq MHz is outside tolerated range ${freqMin}-${freqMax}"
       |  exit 1
       |}
       |puts "Achieve frequency $$freq MHz phase $$phase degrees jitter $$jitter ps"
       |""".stripMargin
  }.mkString


  val aligned = if (c.input.feedback) " CONFIG.USE_PHASE_ALIGNMENT {true} \\\n" else ""

  ElaborationArtefacts.add(s"${moduleName}.vivado.tcl",
    s"""create_ip -name clk_wiz -vendor xilinx.com -library ip -module_name \\
       | ${moduleName} -dir $$ipdir -force
       |set_property -dict [list \\
       | CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \\
       | CONFIG.PRIM_SOURCE {Differential_clock_capable_pin} \\
       | CONFIG.NUM_OUT_CLKS {${c.req.size.toString}} \\
       | CONFIG.PRIM_IN_FREQ {${c.input.freqMHz.toString}} \\
       | CONFIG.CLKIN1_JITTER_PS {${c.input.jitter}} \\
       |${used}${aligned}${outputs}] [get_ips ${moduleName}]
       |set mult [get_property CONFIG.MMCM_CLKFBOUT_MULT_F [get_ips ${moduleName}]]
       |set div1 [get_property CONFIG.MMCM_DIVCLK_DIVIDE [get_ips ${moduleName}]]
       |${checks}""".stripMargin)
}

//IP : xilinx pll with differential input clock
class DiffSeries7PLL(c : PLLParameters) extends BlackBox with PLLInstance {
  val io = IO(new Bundle {
    val clk_in1_p   = Input(Clock())
    val clk_in1_n   = Input(Clock())
    val clk_out1  = if (c.req.size >= 1) Some(Output(Clock())) else None
    val clk_out2  = if (c.req.size >= 2) Some(Output(Clock())) else None
    val clk_out3  = if (c.req.size >= 3) Some(Output(Clock())) else None
    val clk_out4  = if (c.req.size >= 4) Some(Output(Clock())) else None
    val clk_out5  = if (c.req.size >= 5) Some(Output(Clock())) else None
    val clk_out6  = if (c.req.size >= 6) Some(Output(Clock())) else None
    val clk_out7  = if (c.req.size >= 7) Some(Output(Clock())) else None
    val reset     = Input(Bool())
    val locked    = Output(Bool())
  })

  val moduleName = c.name
  override def desiredName = c.name

  def getClocks = Seq() ++ io.clk_out1 ++ io.clk_out2 ++
    io.clk_out3 ++ io.clk_out4 ++
    io.clk_out5 ++ io.clk_out6 ++
    io.clk_out7
  def getInput = io.clk_in1_p
  def getReset = Some(io.reset)
  def getLocked = io.locked
  def getClockNames = Seq.tabulate (c.req.size) { i =>
    s"${c.name}/inst/mmcm_adv_inst/CLKOUT${i}"
  }

  val used = Seq.tabulate(7) { i =>
    s" CONFIG.CLKOUT${i+1}_USED {${i < c.req.size}} \\\n"
  }.mkString

  val outputs = c.req.zipWithIndex.map { case (r, i) =>
    s""" CONFIG.CLKOUT${i+1}_REQUESTED_OUT_FREQ {${r.freqMHz}} \\
       | CONFIG.CLKOUT${i+1}_REQUESTED_PHASE {${r.phaseDeg}} \\
       | CONFIG.CLKOUT${i+1}_REQUESTED_DUTY_CYCLE {${r.dutyCycle}} \\
       |""".stripMargin
  }.mkString

  val checks = c.req.zipWithIndex.map { case (r, i) =>
    val f = if (i == 0) "_F" else ""
    val phaseMin = r.phaseDeg - r.phaseErrorDeg
    val phaseMax = r.phaseDeg + r.phaseErrorDeg
    val freqMin = r.freqMHz * (1 - r.freqErrorPPM / 1000000)
    val freqMax = r.freqMHz * (1 + r.freqErrorPPM / 1000000)
    s"""set jitter [get_property CONFIG.CLKOUT${i+1}_JITTER [get_ips ${moduleName}]]
       |if {$$jitter > ${r.jitterPS}} {
       |  puts "Output jitter $$jitter ps exceeds required limit of ${r.jitterPS}"
       |  exit 1
       |}
       |set phase [get_property CONFIG.MMCM_CLKOUT${i}_PHASE [get_ips ${moduleName}]]
       |if {$$phase < ${phaseMin} || $$phase > ${phaseMax}} {
       |  puts "Achieved phase $$phase degrees is outside tolerated range ${phaseMin}-${phaseMax}"
       |  exit 1
       |}
       |set div2 [get_property CONFIG.MMCM_CLKOUT${i}_DIVIDE${f} [get_ips ${moduleName}]]
       |set freq [expr { ${c.input.freqMHz} * $$mult / $$div1 / $$div2 }]
       |if {$$freq < ${freqMin} || $$freq > ${freqMax}} {
       |  puts "Achieved frequency $$freq MHz is outside tolerated range ${freqMin}-${freqMax}"
       |  exit 1
       |}
       |puts "Achieve frequency $$freq MHz phase $$phase degrees jitter $$jitter ps"
       |""".stripMargin
  }.mkString


  val aligned = if (c.input.feedback) " CONFIG.USE_PHASE_ALIGNMENT {true} \\\n" else ""

  ElaborationArtefacts.add(s"${moduleName}.vivado.tcl",
    s"""create_ip -name clk_wiz -vendor xilinx.com -library ip -module_name \\
       | ${moduleName} -dir $$ipdir -force
       |set_property -dict [list CONFIG.PRIMITIVE {PLL} \\
       | CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \\
       | CONFIG.PRIM_SOURCE {Differential_clock_capable_pin} \\
       | CONFIG.NUM_OUT_CLKS {${c.req.size.toString}} \\
       | CONFIG.PRIM_IN_FREQ {${c.input.freqMHz.toString}} \\
       | CONFIG.CLKIN1_JITTER_PS {${c.input.jitter}} \\
       |${used}${aligned}${outputs}] [get_ips ${moduleName}]
       |set mult [get_property CONFIG.MMCM_CLKFBOUT_MULT_F [get_ips ${moduleName}]]
       |set div1 [get_property CONFIG.MMCM_DIVCLK_DIVIDE [get_ips ${moduleName}]]
       |${checks}""".stripMargin)
}

class RESETSYS extends BlackBox {
  val io = IO(new Bundle {
    val slowest_sync_clk     = Input(Clock())
    val ext_reset_in         = Input(Bool())
    val aux_reset_in         = Input(Bool())
    val mb_debug_sys_rst     = Input(Bool())
    val dcm_locked           = Input(Bool())
    val mb_reset             = Output(Bool())
    val bus_struct_reset     = Output(Bool())
    val peripheral_reset     = Output(Bool())
    val interconnect_aresetn = Output(Bool())
    val peripheral_aresetn   = Output(Bool())
  })
  ElaborationArtefacts.add(
    "resetsys.vivado.tcl",
    """create_ip -vendor xilinx.com -library ip -name proc_sys_reset -module_name RESETSYS -dir $ipdir -force
    set_property -dict [list \
            CONFIG.C_EXT_RESET_HIGH {true} \
            CONFIG.C_AUX_RESET_HIGH {true} \
            CONFIG.C_NUM_BUS_RST {1} \
            CONFIG.C_NUM_PERP_RST {1} \
            CONFIG.C_NUM_INTERCONNECT_ARESETN {1} \
            CONFIG.C_NUM_PERP_ARESETN {1} \
            ] [get_ips RESETSYS] """
  )
}
/*
   Copyright 2016 SiFive, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
