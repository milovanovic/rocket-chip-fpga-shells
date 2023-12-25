package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental.dataview._
import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config._
import sifive.fpgashells.clocks._
import sifive.fpgashells.devices.xilinx.xilinxnexysvideomig._
import devices.xilinx.xilinxnexysvideodeserializer._
import dspblocks.testchain.DSPChainKey
import dspblocks.toplevel.TopLevelKey
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.shell._

class SysClockNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: ClockInputDesignInput, val shellInput: ClockInputShellInput)
  extends SingleEndedClockInputXilinxPlacedOverlay(name, designInput, shellInput)
{
  val node = shell { ClockSourceNode(freqMHz = 100, jitterPS = 50) }

  shell { InModuleBody {
    val clk: Clock = io
    shell.xdc.addPackagePin(clk, "R4") // Sch=sysclk
    shell.xdc.addIOStandard(clk, "LVCMOS33")
  } }
}
class SysClockNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: ClockInputShellInput)(implicit val valName: ValName)
  extends ClockInputShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: ClockInputDesignInput) = new SysClockNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// CAUTION: PMOD JA also used for SDIO
// PMOD JA used for JTAG
class JTAGDebugNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: JTAGDebugDesignInput, val shellInput: JTAGDebugShellInput)
  extends JTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))
    val packagePinsWithPackageIOs = Seq(
      ("AB20", IOPin(io.jtag_TCK)),  // Sch=ja[3]
      ("AA21", IOPin(io.jtag_TMS)),  // Sch=ja[8]
      ("Y21",  IOPin(io.jtag_TDI)),  // Sch=ja[7]
      ("AB22", IOPin(io.jtag_TDO)),  // Sch=ja[1]
      ("AA20", IOPin(io.srst_n)))    // Sch=ja[9]

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addPullup(io)
    } }
  } }
}
class JTAGDebugNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: JTAGDebugShellInput)(implicit val valName: ValName)
  extends JTAGDebugShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: JTAGDebugDesignInput) = new JTAGDebugNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// CAUTION: PMOD JA also used for SDIO
// PMOD JA used for cJTAG
class cJTAGDebugNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: cJTAGDebugDesignInput, val shellInput: cJTAGDebugShellInput)
  extends cJTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCKC", IOPin(io.cjtag_TCKC), 10)
    shell.sdc.addGroup(clocks = Seq("JTCKC"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.cjtag_TCKC))
    val packagePinsWithPackageIOs = Seq(
      ("AB20", IOPin(io.cjtag_TCKC)), // Sch=ja[3]
      ("AA21", IOPin(io.cjtag_TMSC)), // Sch=ja[8]
      ("AA20", IOPin(io.srst_n)))     // Sch=ja[9]

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
    } }
      shell.xdc.addPullup(IOPin(io.cjtag_TCKC))
      shell.xdc.addPullup(IOPin(io.srst_n))
  } }
}
class cJTAGDebugNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: cJTAGDebugShellInput)(implicit val valName: ValName)
  extends cJTAGDebugShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: cJTAGDebugDesignInput) = new cJTAGDebugNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// CAUTION: PMOD JA also used for JTAG/cJTAG
// PMOD JA used for SDIO
class SDIONexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: SPIDesignInput, val shellInput: SPIShellInput)
  extends SDIOXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(
      ("AB18", IOPin(io.spi_clk)),    // Sch=ja[4]
      ("AB21", IOPin(io.spi_cs)),     // Sch=ja[2]
      ("AB20", IOPin(io.spi_dat(0))), // Sch=ja[3]
      ("Y21",  IOPin(io.spi_dat(1))), // Sch=ja[7]
      ("AA21", IOPin(io.spi_dat(2))), // Sch=ja[8]
      ("AB22", IOPin(io.spi_dat(3)))) // Sch=ja[1]

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addIOB(io)
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}
class SDIONexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: SPIShellInput)(implicit val valName: ValName)
  extends SPIShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: SPIDesignInput) = new SDIONexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class SPIFlashNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: SPIFlashDesignInput, val shellInput: SPIFlashShellInput)
  extends SPIFlashXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(
      ("L12", IOPin(io.qspi_sck)),   // Sch=CCLK
      ("T19", IOPin(io.qspi_cs)),    // Sch=qspi_cs
      ("P22", IOPin(io.qspi_dq(0))), // Sch=qspi_dq[0]
      ("R22", IOPin(io.qspi_dq(1))), // Sch=qspi_dq[1]
      ("P21", IOPin(io.qspi_dq(2))), // Sch=qspi_dq[2]
      ("R21", IOPin(io.qspi_dq(3)))) // Sch=qspi_dq[3]

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}
class SPIFlashNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: SPIFlashShellInput)(implicit val valName: ValName)
  extends SPIFlashShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: SPIFlashDesignInput) = new SPIFlashNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// PMOD JC used for Trace
class TracePMODNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: TracePMODDesignInput, val shellInput: TracePMODShellInput)
  extends TracePMODXilinxPlacedOverlay(name, designInput, shellInput, packagePins = Seq(
    "Y6",  // Sch=jc_p[1]
    "AA6", // Sch=jc_n[1]
    "AA8", // Sch=jc_p[2]
    "AB8", // Sch=jc_n[2]
    "R6",  // Sch=jc_p[3]
    "T6",  // Sch=jc_n[3]
    "AB7", // Sch=jc_p[4]
    "AB6"  // Sch=jc_n[4]
  )
)
class TracePMODNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: TracePMODShellInput)(implicit val valName: ValName)
  extends TracePMODShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: TracePMODDesignInput) = new TracePMODNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// PMOD JB used for GPIO
class GPIOPMODNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: GPIOPMODDesignInput, val shellInput: GPIOPMODShellInput)
  extends GPIOPMODXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(
      ("V9", IOPin(io.gpio_pmod_0)), // Sch=jb_p[1]
      ("V8", IOPin(io.gpio_pmod_1)), // Sch=jb_n[1]
      ("V7", IOPin(io.gpio_pmod_2)), // Sch=jb_p[2]
      ("W7", IOPin(io.gpio_pmod_3)), // Sch=jb_n[2]
      ("W9", IOPin(io.gpio_pmod_4)), // Sch=jb_p[3]
      ("Y9", IOPin(io.gpio_pmod_5)), // Sch=jb_n[3]
      ("Y8", IOPin(io.gpio_pmod_6)), // Sch=jb_p[4]
      ("Y7", IOPin(io.gpio_pmod_7))) // Sch=jb_n[4]

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
    } }
    packagePinsWithPackageIOs drop 7 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}
class GPIOPMODNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: GPIOPMODShellInput)(implicit val valName: ValName)
  extends GPIOPMODShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: GPIOPMODDesignInput) = new GPIOPMODNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class UARTNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTXilinxPlacedOverlay(name, designInput, shellInput, false)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(
      ("V18", IOPin(io.rxd)), // Sch=uart_txd_in
      ("AA19", IOPin(io.txd)) // Sch=uart_rxd_out
    )

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addIOB(io)
    } }
  } }
}
class UARTNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: UARTDesignInput) = new UARTNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// LVDS
class LVDSNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: LVDSDesignInput, val shellInput: LVDSShellInput)
  extends LVDSXilinxPlacedOverlay(name, designInput, shellInput)
{
  val deser: XilinxNexysVideoDeserializer = LazyModule(new XilinxNexysVideoDeserializer(XilinxNexysVideoDeserializerParams(si.channels, si.chips)))

  shell { InModuleBody {
    val packagePinsWithPackageIOs =  if (si.chips == 1) {
      // AWR2243 pins
      Seq(
        ("B17", IOPin(io.lvds.head.i_clk_p)),     // Sch=FMC_LA17_CC_P
        ("B18", IOPin(io.lvds.head.i_clk_n)),     // Sch=FMC_LA17_CC_N
        ("M13", IOPin(io.lvds.head.i_valid_p)),   // Sch=FMC_LA07_P
        ("L13", IOPin(io.lvds.head.i_valid_n)),   // Sch=FMC_LA07_N
        ("A18", IOPin(io.lvds.head.i_frame_p)),   // Sch=FMC_LA19_P
        ("A19", IOPin(io.lvds.head.i_frame_n)),   // Sch=FMC_LA19_N
        ("L16", IOPin(io.lvds.head.i_data_p(3))), // Sch=FMC_LA15_P
        ("K16", IOPin(io.lvds.head.i_data_n(3))), // Sch=FMC_LA15_N
        ("G17", IOPin(io.lvds.head.i_data_p(2))), // Sch=FMC_LA16_P
        ("G18", IOPin(io.lvds.head.i_data_n(2))), // Sch=FMC_LA16_N
        ("L14", IOPin(io.lvds.head.i_data_p(1))), // Sch=FMC_LA11_P
        ("L15", IOPin(io.lvds.head.i_data_n(1))), // Sch=FMC_LA11_N
        ("L19", IOPin(io.lvds.head.i_data_p(0))), // Sch=FMC_LA12_P
        ("L20", IOPin(io.lvds.head.i_data_n(0)))  // Sch=FMC_LA12_N
      )
    }
    else {
      // MMWCAS-RF pins
      Seq(
        ("B17", IOPin(io.lvds.head.i_clk_p)),     // Sch=IO_L11P_T1_SRCC_16
        ("B18", IOPin(io.lvds.head.i_clk_n)),     // Sch=IO_L11N_T1_SRCC_16
        ("F18", IOPin(io.lvds.head.i_valid_p)),   // Sch=IO_L15P_T2_DQS_16
        ("E18", IOPin(io.lvds.head.i_valid_n)),   // Sch=IO_L15N_T2_DQS_16
        ("F16", IOPin(io.lvds.head.i_frame_p)),   // Sch=IO_L2P_T0_16
        ("E17", IOPin(io.lvds.head.i_frame_n)),   // Sch=IO_L2N_T0_16
        ("F19", IOPin(io.lvds.head.i_data_p(0))), // Sch=IO_L18P_T2_16
        ("F20", IOPin(io.lvds.head.i_data_n(0))), // Sch=IO_L18N_T2_16
        ("A18", IOPin(io.lvds.head.i_data_p(1))), // Sch=IO_L17P_T2_16
        ("A19", IOPin(io.lvds.head.i_data_n(1))), // Sch=IO_L17N_T2_16
        ("D17", IOPin(io.lvds.head.i_data_p(2))), // Sch=IO_L12P_T1_MRCC_16
        ("C17", IOPin(io.lvds.head.i_data_n(2))), // Sch=IO_L12N_T1_MRCC_16
        ("E21", IOPin(io.lvds.head.i_data_p(3))), // Sch=IO_L23P_T3_16
        ("D21", IOPin(io.lvds.head.i_data_n(3))), // Sch=IO_L23N_T3_16

        ("K17", IOPin(io.lvds(1).i_clk_p)),     // Sch=IO_L11P_T1_SRCC_16
        ("J17", IOPin(io.lvds(1).i_clk_n)),     // Sch=IO_L21P_T3_DQS_15
        ("M13", IOPin(io.lvds(1).i_valid_p)),   // Sch=IO_L20P_T3_A20_15
        ("L13", IOPin(io.lvds(1).i_valid_n)),   // Sch=IO_L20N_T3_A19_15
        ("M18", IOPin(io.lvds(1).i_frame_p)),   // Sch=IO_L16P_T2_A28_15
        ("L18", IOPin(io.lvds(1).i_frame_n)),   // Sch=IO_L16N_T2_A27_15
        ("J20", IOPin(io.lvds(1).i_data_p(0))), // Sch=IO_L11P_T1_SRCC_15
        ("J21", IOPin(io.lvds(1).i_data_n(0))), // Sch=IO_L11N_T1_SRCC_15
        ("N22", IOPin(io.lvds(1).i_data_p(1))), // Sch=IO_L15P_T2_DQS_15
        ("M22", IOPin(io.lvds(1).i_data_n(1))), // Sch=IO_L15N_T2_DQS_ADV_B_15
        ("H20", IOPin(io.lvds(1).i_data_p(2))), // Sch=IO_L8P_T1_AD10P_15
        ("G20", IOPin(io.lvds(1).i_data_n(2))), // Sch=IO_L8N_T1_AD10N_15
        ("L19", IOPin(io.lvds(1).i_data_p(3))), // Sch=IO_L14P_T2_SRCC_15
        ("L20", IOPin(io.lvds(1).i_data_n(3)))  // Sch=IO_L14N_T2_SRCC_15
      )
    }

    packagePinsWithPackageIOs foreach { case (pin, io) =>
      shell.xdc.addDiffIOStandard(io, pin, standard = "LVDS_25", diffTerm = true)
    }
    if (si.chips == 1) shell.sdc.addGroup(customPins = Seq("deser_pll_0/clk_out1", "deser_pll_0/clk_out2"))
    else {
      shell.sdc.addGroup(customPins = Seq("deser_pll_0/clk_out1", "deser_pll_0/clk_out2"))
      shell.sdc.addGroup(customPins = Seq("deser_pll_1/clk_out1", "deser_pll_1/clk_out2"))
      shell.xdc.addClockDedicatedRoute("deser_pll_0/inst/clk_in1_deser_pll")
      shell.xdc.addClockDedicatedRoute("deser_pll_1/inst/clk_in1_deser_pll")
    }
  } }
}
class LVDSNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: LVDSShellInput)(implicit val valName: ValName)
  extends LVDSShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: LVDSDesignInput) = new LVDSNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// Ethernet
class ETHNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: ETHDesignInput, val shellInput: ETHShellInput)
  extends ETHXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(
      ("AA16", IOPin(io.mdc)),          // Sch=ETH_MDC
      ("Y16",  IOPin(io.mdio)),         // Sch=ETH_MDIO
      ("AB16", IOPin(io.rgmii_rxd(0))), // Sch=ETH_RXD0
      ("AA15", IOPin(io.rgmii_rxd(1))), // Sch=ETH_RXD1
      ("AB15", IOPin(io.rgmii_rxd(2))), // Sch=ETH_RXD2
      ("AB11", IOPin(io.rgmii_rxd(3))), // Sch=ETH_RXD3
      ("Y12",  IOPin(io.rgmii_txd(0))), // Sch=ETH_TXD0
      ("W12",  IOPin(io.rgmii_txd(1))), // Sch=ETH_TXD1
      ("W11",  IOPin(io.rgmii_txd(2))), // Sch=ETH_TXD2
      ("Y11",  IOPin(io.rgmii_txd(3))), // Sch=ETH_TXD3
      ("AA14", IOPin(io.rgmii_txc)),    // Sch=ETH_TXCK
      ("V10",  IOPin(io.rgmii_tx_ctl)), // Sch=ETH_TXCTL
      ("V13",  IOPin(io.rgmii_rxc)),    // Sch=ETH_RXCK
      ("W10",  IOPin(io.rgmii_rx_ctl))  // Sch=ETH_RXCTL
    )
    packagePinsWithPackageIOs foreach { case (pin, io) =>
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, standard = "LVCMOS25")
    }
    // Ethernet clock
    shell.sdc.addClock("rgmii_rxc", IOPin(io.rgmii_rxc), 125)
    shell.sdc.addGroup(clocks = Seq("rgmii_rxc"))
    // Ethernet reset is on bank 34 which has VCC=3.3V
    shell.xdc.addPackagePin(IOPin(io.phy_resetn), "U7")
    shell.xdc.addIOStandard(IOPin(io.phy_resetn), standard = "LVCMOS33")


  } }
}
class ETHNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: ETHShellInput)(implicit val valName: ValName)
  extends ETHShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: ETHDesignInput) = new ETHNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// Ethernet
class TopLevelNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: TopLevelDesignInput, val shellInput: TopLevelShellInput)
  extends TopLevelXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(
      ("AA16", IOPin(io.mdc)),          // Sch=ETH_MDC
      ("Y16",  IOPin(io.mdio)),         // Sch=ETH_MDIO
      ("AB16", IOPin(io.rgmii_rxd(0))), // Sch=ETH_RXD0
      ("AA15", IOPin(io.rgmii_rxd(1))), // Sch=ETH_RXD1
      ("AB15", IOPin(io.rgmii_rxd(2))), // Sch=ETH_RXD2
      ("AB11", IOPin(io.rgmii_rxd(3))), // Sch=ETH_RXD3
      ("Y12",  IOPin(io.rgmii_txd(0))), // Sch=ETH_TXD0
      ("W12",  IOPin(io.rgmii_txd(1))), // Sch=ETH_TXD1
      ("W11",  IOPin(io.rgmii_txd(2))), // Sch=ETH_TXD2
      ("Y11",  IOPin(io.rgmii_txd(3))), // Sch=ETH_TXD3
      ("AA14", IOPin(io.rgmii_txc)),    // Sch=ETH_TXCK
      ("V10",  IOPin(io.rgmii_tx_ctl)), // Sch=ETH_TXCTL
      ("V13",  IOPin(io.rgmii_rxc)),    // Sch=ETH_RXCK
      ("W10",  IOPin(io.rgmii_rx_ctl))  // Sch=ETH_RXCTL
    )
    packagePinsWithPackageIOs foreach { case (pin, io) =>
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, standard = "LVCMOS25")
    }
    // Ethernet clock
    shell.sdc.addClock("rgmii_rxc", IOPin(io.rgmii_rxc), 125)
    shell.sdc.addGroup(clocks = Seq("rgmii_rxc"))
    // Ethernet reset is on bank 34 which has VCC=3.3V
    shell.xdc.addPackagePin(IOPin(io.phy_resetn), "U7")
    shell.xdc.addIOStandard(IOPin(io.phy_resetn), standard = "LVCMOS33")

    val lvdsIOs = Seq(
        ("B18", IOPin(io.io_2_lvds_clk_n)), // Sch=IO_L11P_T1_SRCC_16
        ("B17", IOPin(io.io_2_lvds_clk_p)), // Sch=IO_L11N_T1_SRCC_16
        ("E18", IOPin(io.io_2_lvds_valid_n)), // Sch=IO_L15P_T2_DQS_16
        ("F18", IOPin(io.io_2_lvds_valid_p)), // Sch=IO_L15N_T2_DQS_16
        ("E17", IOPin(io.io_2_lvds_frame_clk_n)), // Sch=IO_L2P_T0_16
        ("F16", IOPin(io.io_2_lvds_frame_clk_p)), // Sch=IO_L2N_T0_16
        ("F20", IOPin(io.io_2_lvds_data_n(0))), // Sch=IO_L18P_T2_16
        ("F19", IOPin(io.io_2_lvds_data_p(0))), // Sch=IO_L18N_T2_16
        ("A19", IOPin(io.io_2_lvds_data_n(1))), // Sch=IO_L17P_T2_16
        ("A18", IOPin(io.io_2_lvds_data_p(1))), // Sch=IO_L17N_T2_16
        ("C17", IOPin(io.io_2_lvds_data_n(2))), // Sch=IO_L12P_T1_MRCC_16
        ("D17", IOPin(io.io_2_lvds_data_p(2))), // Sch=IO_L12N_T1_MRCC_16
        ("D21", IOPin(io.io_2_lvds_data_n(3))), // Sch=IO_L23P_T3_16
        ("E21", IOPin(io.io_2_lvds_data_p(3))), // Sch=IO_L23N_T3_16

        ("J17", IOPin(io.io_3_lvds_clk_n)), // Sch=IO_L11P_T1_SRCC_16
        ("K17", IOPin(io.io_3_lvds_clk_p)), // Sch=IO_L21P_T3_DQS_15
        ("L13", IOPin(io.io_3_lvds_valid_n)), // Sch=IO_L20P_T3_A20_15
        ("M13", IOPin(io.io_3_lvds_valid_p)), // Sch=IO_L20N_T3_A19_15
        ("L18", IOPin(io.io_3_lvds_frame_clk_n)), // Sch=IO_L16P_T2_A28_15
        ("M18", IOPin(io.io_3_lvds_frame_clk_p)), // Sch=IO_L16N_T2_A27_15
        ("J21", IOPin(io.io_3_lvds_data_n(0))), // Sch=IO_L11P_T1_SRCC_15
        ("J20", IOPin(io.io_3_lvds_data_p(0))), // Sch=IO_L11N_T1_SRCC_15
        ("M22", IOPin(io.io_3_lvds_data_n(1))), // Sch=IO_L15P_T2_DQS_15
        ("N22", IOPin(io.io_3_lvds_data_p(1))), // Sch=IO_L15N_T2_DQS_ADV_B_15
        ("G20", IOPin(io.io_3_lvds_data_n(2))), // Sch=IO_L8P_T1_AD10P_15
        ("H20", IOPin(io.io_3_lvds_data_p(2))), // Sch=IO_L8N_T1_AD10N_15
        ("L20", IOPin(io.io_3_lvds_data_n(3))), // Sch=IO_L14P_T2_SRCC_15
        ("L19", IOPin(io.io_3_lvds_data_p(3))) // Sch=IO_L14N_T2_SRCC_15
      )
    lvdsIOs foreach { case (pin, io) =>
      shell.xdc.addDiffIOStandard(io, pin, standard = "LVDS_25", diffTerm = true)
    }

    val ctrl1IOs = Seq(
      ("B15", IOPin(io.awr_host_intr1_fmc)),
      ("N20", IOPin(io.awr_host_intr2_fmc)),
      ("N19", IOPin(io.awr_host_intr3_fmc)),
      ("N18", IOPin(io.awr_host_intr4_fmc)),
      ("D19", IOPin(io.awr_spi_cs1_fmc)),
      ("E19", IOPin(io.awr_spi_cs2_fmc)),
      ("M21", IOPin(io.awr_spi_cs3_fmc)),
      ("B16", IOPin(io.awr_spi_cs4_fmc)),
      ("K21", IOPin(io.awr_spi_miso_fmc)),
      ("M20", IOPin(io.awr_spi_mosi_fmc)),
      ("K22", IOPin(io.awr_spi_clk_fmc)),
      ("L21", IOPin(io.awr_nrst2_fmc))
    )
    val ctrl2IOs = Seq(
      ("V9", IOPin(io.awr_host_intr1)),
      ("V7", IOPin(io.awr_host_intr2)),
      ("W9", IOPin(io.awr_host_intr3)),
      ("Y8", IOPin(io.awr_host_intr4)),
      ("AB22", IOPin(io.awr_spi_cs1)),
      ("AB21", IOPin(io.awr_spi_cs2)),
      ("Y21", IOPin(io.awr_spi_cs3)),
      ("AA21", IOPin(io.awr_spi_cs4)),
      ("AB20", IOPin(io.awr_spi_miso)),
      ("AA20", IOPin(io.awr_spi_mosi)),
      ("AB18", IOPin(io.awr_spi_clk)),
      ("Y6",  IOPin(io.awr_nrst1)),
      ("AA8", IOPin(io.awr_nrst2)),
      ("R6", IOPin(io.awr_nrst1_pmod))
    )
    ctrl1IOs foreach { case (pin, io) =>
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, standard = "LVCMOS25")
    }
    ctrl2IOs foreach { case (pin, io) =>
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, standard = "LVCMOS33")
    }
  } }
}
class TopLevelNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: TopLevelShellInput)(implicit val valName: ValName)
  extends TopLevelShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: TopLevelDesignInput) = new TopLevelNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// 8 LEDs
object LEDNexysVideoPinConstraints{
  val pins = Seq(
    "T14", // Sch=led[0]
    "T15", // Sch=led[1]
    "T16", // Sch=led[2]
    "U16", // Sch=led[3]
    "V15", // Sch=led[4]
    "W16", // Sch=led[5]
    "W15", // Sch=led[6]
    "Y13"  // Sch=led[7]
  )
}
class LEDNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: LEDDesignInput, val shellInput: LEDShellInput)
  extends LEDXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(LEDNexysVideoPinConstraints.pins(shellInput.number)), ioStandard = "LVCMOS25")
class LEDNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: LEDShellInput)(implicit val valName: ValName)
  extends LEDShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: LEDDesignInput) = new LEDNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// 8 Switches
object SwitchNexysVideoPinConstraints{
  val pins = Seq(
    "E22", // Sch=sw[0]
    "F21", // Sch=sw[1]
    "G21", // Sch=sw[2]
    "G22", // Sch=sw[3]
    "H17", // Sch=sw[4]
    "J16", // Sch=sw[5]
    "K13", // Sch=sw[6]
    "M17"  // Sch=sw[7]
  )
}
class SwitchNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: SwitchDesignInput, val shellInput: SwitchShellInput)
  extends SwitchXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(SwitchNexysVideoPinConstraints.pins(shellInput.number)))
class SwitchNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: SwitchShellInput)(implicit val valName: ValName)
  extends SwitchShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: SwitchDesignInput) = new SwitchNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// 5 Buttons (cpu_resetn excluded)
object ButtonNexysVideoPinConstraints {
  val pins = Seq(
    "B22", // Sch=btnc
    "D22", // Sch=btnd
    "C22", // Sch=btnl
    "D14", // Sch=btnr
    "F15"  // Sch=btnu
  )
}
class ButtonNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: ButtonDesignInput, val shellInput: ButtonShellInput)
  extends ButtonXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(ButtonNexysVideoPinConstraints.pins(shellInput.number)))
class ButtonNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: ButtonShellInput)(implicit val valName: ValName)
  extends ButtonShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: ButtonDesignInput) = new ButtonNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class JTAGDebugBScanNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: JTAGDebugBScanDesignInput, val shellInput: JTAGDebugBScanShellInput)
 extends JTAGDebugBScanXilinxPlacedOverlay(name, designInput, shellInput)
class JTAGDebugBScanNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: JTAGDebugBScanShellInput)(implicit val valName: ValName)
  extends JTAGDebugBScanShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: JTAGDebugBScanDesignInput) = new JTAGDebugBScanNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

case object NexysVideoDDRSize extends Field[BigInt](0x20000000L * 1) // 512 MB
class DDRNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: DDRDesignInput, val shellInput: DDRShellInput)
  extends DDRPlacedOverlay[XilinxNexysVideoMIGPads](name, designInput, shellInput)
{
  val size = p(NexysVideoDDRSize)

  val ddrClk1 = shell { ClockSinkNode(freqMHz = 100)}
  val ddrClk2 = shell { ClockSinkNode(freqMHz = 200)}
  val ddrGroup = shell { ClockGroup() }
  ddrClk1 := di.wrangler := ddrGroup := di.corePLL
  ddrClk2 := di.wrangler := ddrGroup
  
  val migParams = XilinxNexysVideoMIGParams(address = AddressSet.misaligned(di.baseAddress, size), beatBytes = di.beatBytes)
  val mig = LazyModule(new XilinxNexysVideoMIG(migParams))
  val ddrUI     = shell { ClockSourceNode(freqMHz = 100) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := di.wrangler := ddrUI

  def overlayOutput = DDROverlayOutput(ddr = mig.node)
  def ioFactory = new XilinxNexysVideoMIGPads(size)

  shell { InModuleBody {
    require (shell.sys_clock.get.isDefined, "Use of DDRNexysVideoPlacedOverlay depends on SysClockNexysVideoPlacedOverlay")
    val (sys, _) = shell.sys_clock.get.get.overlayOutput.node.out(0)
    val (ui, _) = ddrUI.out(0)
    val (dclk1, _) = ddrClk1.in(0)
    val (dclk2, _) = ddrClk2.in(0)
    val (ar, _) = areset.in(0)
    val port = mig.module.io.port
    
    io <> port.viewAsSupertype(new XilinxNexysVideoMIGPads(mig.depth))
    ui.clock := port.ui_clk
    ui.reset := !port.mmcm_locked || port.ui_clk_sync_rst
    port.sys_clk_i := dclk1.clock.asUInt
    port.clk_ref_i := dclk2.clock.asUInt
    port.sys_rst := shell.pllReset
    port.aresetn := !ar.reset
  } }

  shell.sdc.addGroup(clocks = Seq("clk_pll_i"), pins = Seq(mig.island.module.blackbox.io.ui_clk))
}
class DDRNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: DDRShellInput)(implicit val valName: ValName)
  extends DDRShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: DDRDesignInput) = new DDRNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// Core to shell external resets
class CTSResetNexysVideoPlacedOverlay(val shell: NexysVideoShellBasicOverlays, name: String, val designInput: CTSResetDesignInput, val shellInput: CTSResetShellInput)
  extends CTSResetPlacedOverlay(name, designInput, shellInput)
class CTSResetNexysVideoShellPlacer(val shell: NexysVideoShellBasicOverlays, val shellInput: CTSResetShellInput)(implicit val valName: ValName)
  extends CTSResetShellPlacer[NexysVideoShellBasicOverlays] {
  def place(designInput: CTSResetDesignInput) = new CTSResetNexysVideoPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// Optional DDR
case object NexysVideoShellDDR extends Field[Boolean](true)
class WithNoNexysVideoShellDDR extends Config((site, here, up) => {
  case NexysVideoShellDDR => false
})

abstract class NexysVideoShellBasicOverlays()(implicit p: Parameters) extends Series7Shell {
  // Order matters; ddr depends on sys_clock
  val sys_clock = Overlay(ClockInputOverlayKey, new SysClockNexysVideoShellPlacer(this, ClockInputShellInput()))
  val led       = Seq.tabulate(8)(i => Overlay(LEDOverlayKey, new LEDNexysVideoShellPlacer(this, LEDMetas(i))(valName = ValName(s"led_$i"))))
  val switch    = Seq.tabulate(8)(i => Overlay(SwitchOverlayKey, new SwitchNexysVideoShellPlacer(this, SwitchShellInput(number = i))(valName = ValName(s"switch_$i"))))
  val button    = Seq.tabulate(5)(i => Overlay(ButtonOverlayKey, new ButtonNexysVideoShellPlacer(this, ButtonShellInput(number = i))(valName = ValName(s"button_$i"))))
  val ddr       = if (p(NexysVideoShellDDR)) Some(Overlay(DDROverlayKey, new DDRNexysVideoShellPlacer(this, DDRShellInput()))) else None
  val uart      = Overlay(UARTOverlayKey, new UARTNexysVideoShellPlacer(this, UARTShellInput()))
  val lvds      = if (p(DSPChainKey).isDefined) Some(Overlay(LVDSOverlayKey, new LVDSNexysVideoShellPlacer(this, LVDSShellInput(p(DSPChainKey).get.dataChannels, p(DSPChainKey).get.dataChips)))) else None
  val eth       = if (p(DSPChainKey).isDefined) Some(Overlay(ETHOverlayKey, new ETHNexysVideoShellPlacer(this, ETHShellInput()))) else None
  val top       = if (p(TopLevelKey).isDefined) Some(Overlay(TopLevelOverlayKey, new TopLevelNexysVideoShellPlacer(this, TopLevelShellInput()))) else None
  val sdio      = Overlay(SPIOverlayKey, new SDIONexysVideoShellPlacer(this, SPIShellInput()))
  val jtag      = Overlay(JTAGDebugOverlayKey, new JTAGDebugNexysVideoShellPlacer(this, JTAGDebugShellInput()))
  val cjtag     = Overlay(cJTAGDebugOverlayKey, new cJTAGDebugNexysVideoShellPlacer(this, cJTAGDebugShellInput()))
  val spi_flash = Overlay(SPIFlashOverlayKey, new SPIFlashNexysVideoShellPlacer(this, SPIFlashShellInput()))
  val cts_reset = Overlay(CTSResetOverlayKey, new CTSResetNexysVideoShellPlacer(this, CTSResetShellInput()))
  val jtagBScan = Overlay(JTAGDebugBScanOverlayKey, new JTAGDebugBScanNexysVideoShellPlacer(this, JTAGDebugBScanShellInput()))

  def LEDMetas(i: Int): LEDShellInput =
    LEDShellInput(
      color = "white", // There are no RGB LEDs on Nexys Video board
      rgb = false,
      number = i
    )
}

class NexysVideoShell()(implicit p: Parameters) extends NexysVideoShellBasicOverlays
{
  val resetPin = InModuleBody { Wire(Bool()) }
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_.place(ClockInputDesignInput()))
  override lazy val module = new Impl
  class Impl extends LazyRawModuleImp(this) {

    val reset = IO(Input(Bool()))
    xdc.addBoardPin(reset, "reset")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset
    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockNexysVideoPlacedOverlay) => x.clock
    }
    val powerOnReset = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))

    resetPin := reset_ibuf.io.O

    pllReset := (!reset_ibuf.io.O) || powerOnReset //NexysVideo is active low reset
  }
}

class NexysVideoShellGPIOPMOD()(implicit p: Parameters) extends NexysVideoShellBasicOverlays
//This is the Shell used for coreip NexysVideo builds, with GPIOS and trace signals on the pmods
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val gpio_pmod = Overlay(GPIOPMODOverlayKey, new GPIOPMODNexysVideoShellPlacer(this, GPIOPMODShellInput()))
  val trace_pmod = Overlay(TracePMODOverlayKey, new TracePMODNexysVideoShellPlacer(this, TracePMODShellInput()))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_.place(ClockInputDesignInput()))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addBoardPin(reset, "reset")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockNexysVideoPlacedOverlay) => x.clock
    }
    val powerOnReset = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))
    val ctsReset: Bool = cts_reset.get() match {
      case Some(x: CTSResetNexysVideoPlacedOverlay) => x.designInput.rst
      case None => false.B
    }

    pllReset := (!reset_ibuf.io.O) || powerOnReset || ctsReset // NexysVideo is active low reset
  }
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
