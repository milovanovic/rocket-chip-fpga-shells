package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config._

case class TopLevelShellInput(index: Int = 0)
case class TopLevelDesignInput()(implicit val p: Parameters)
case class TopLevelOverlayOutput()
case object TopLevelOverlayKey extends Field[Seq[DesignPlacer[TopLevelDesignInput, TopLevelShellInput, TopLevelOverlayOutput]]](Nil)
trait TopLevelShellPlacer[Shell] extends ShellPlacer[TopLevelDesignInput, TopLevelShellInput, TopLevelOverlayOutput]

class ShellTopLevelPortIO extends Bundle {
  // Ethernet
  val phy_resetn = Output(Bool())
  val rgmii_txd = Output(Vec(4,Bool()))
  val rgmii_tx_ctl = Output(Bool())
  val rgmii_txc = Output(Bool())
  val rgmii_rxd = Input(Vec(4,Bool()))
  val rgmii_rx_ctl = Input(Bool())
  val rgmii_rxc = Input(Bool())
  val mdio = Analog(1.W)
  val mdc = Output(Bool())
  // LVDS 1
  val io_2_lvds_clk_n = Input(Bool())
  val io_2_lvds_clk_p = Input(Bool())
  val io_2_lvds_data_p = Input(Vec(4,Bool()))
  val io_2_lvds_data_n = Input(Vec(4,Bool()))
  val io_2_lvds_valid_n = Input(Bool())
  val io_2_lvds_valid_p = Input(Bool())
  val io_2_lvds_frame_clk_n = Input(Bool())
  val io_2_lvds_frame_clk_p = Input(Bool())
  // LVDS 2
  val io_3_lvds_clk_n = Input(Bool())
  val io_3_lvds_clk_p = Input(Bool())
  val io_3_lvds_data_p = Input(Vec(4,Bool()))
  val io_3_lvds_data_n = Input(Vec(4,Bool()))
  val io_3_lvds_valid_n = Input(Bool())
  val io_3_lvds_valid_p = Input(Bool())
  val io_3_lvds_frame_clk_n = Input(Bool())
  val io_3_lvds_frame_clk_p = Input(Bool())
  // CTRL 1
  val awr_host_intr1_fmc = Input(Bool())
  val awr_host_intr2_fmc = Input(Bool())
  val awr_host_intr3_fmc = Input(Bool())
  val awr_host_intr4_fmc = Input(Bool())
  val awr_spi_cs1_fmc = Output(Bool())
  val awr_spi_cs2_fmc = Output(Bool())
  val awr_spi_cs3_fmc = Output(Bool())
  val awr_spi_cs4_fmc = Output(Bool())
  val awr_spi_miso_fmc = Input(Bool())
  val awr_spi_mosi_fmc = Output(Bool())
  val awr_spi_clk_fmc = Output(Bool())
  val awr_nrst1_pmod = Output(Bool())
  val awr_nrst2_fmc = Output(Bool())
  // CTRL 2
  val awr_host_intr1 = Output(Bool())
  val awr_host_intr2 = Output(Bool())
  val awr_host_intr3 = Output(Bool())
  val awr_host_intr4 = Output(Bool())
  val awr_spi_cs1 = Input(Bool())
  val awr_spi_cs2 = Input(Bool())
  val awr_spi_cs3 = Input(Bool())
  val awr_spi_cs4 = Input(Bool())
  val awr_spi_miso = Output(Bool())
  val awr_spi_mosi = Input(Bool())
  val awr_spi_clk = Input(Bool())
  val awr_nrst1 = Input(Bool())
  val awr_nrst2 = Input(Bool())
}

abstract class TopLevelPlacedOverlay(val name: String, val di: TopLevelDesignInput, val si: TopLevelShellInput)
  extends IOPlacedOverlay[ShellTopLevelPortIO, TopLevelDesignInput, TopLevelShellInput, TopLevelOverlayOutput]
{
  implicit val p: Parameters = di.p

  def ioFactory = new ShellTopLevelPortIO

  def overlayOutput: TopLevelOverlayOutput = TopLevelOverlayOutput()
}

