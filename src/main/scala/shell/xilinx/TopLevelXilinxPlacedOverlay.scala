package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.util.Cat
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._

abstract class TopLevelXilinxPlacedOverlay(name: String, di: TopLevelDesignInput, si: TopLevelShellInput)
  extends TopLevelPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  shell {
    InModuleBody {
      // Ethernet
      io.phy_resetn := tlSink.bundle.phy_resetn
      io.rgmii_txd := tlSink.bundle.rgmii_txd
      io.rgmii_tx_ctl := tlSink.bundle.rgmii_tx_ctl
      io.rgmii_txc := tlSink.bundle.rgmii_txc
      tlSink.bundle.rgmii_rxd := io.rgmii_rxd
      tlSink.bundle.rgmii_rx_ctl := io.rgmii_rx_ctl
      tlSink.bundle.rgmii_rxc := io.rgmii_rxc
//      tlSink.bundle.mdio <> nexys.pinsOverlay.get.tlSink.bundle.mdio
      io.mdc := tlSink.bundle.mdc
      // LVDS 1
      tlSink.bundle.io_2_lvds_clk_n := io.io_2_lvds_clk_n
      tlSink.bundle.io_2_lvds_clk_p := io.io_2_lvds_clk_p
      tlSink.bundle.io_2_lvds_data_p := io.io_2_lvds_data_p
      tlSink.bundle.io_2_lvds_data_n := io.io_2_lvds_data_n
      tlSink.bundle.io_2_lvds_valid_n := io.io_2_lvds_valid_n
      tlSink.bundle.io_2_lvds_valid_p := io.io_2_lvds_valid_p
      tlSink.bundle.io_2_lvds_frame_clk_n := io.io_2_lvds_frame_clk_n
      tlSink.bundle.io_2_lvds_frame_clk_p := io.io_2_lvds_frame_clk_p
      // LVDS 2
      tlSink.bundle.io_3_lvds_clk_n := io.io_3_lvds_clk_n
      tlSink.bundle.io_3_lvds_clk_p := io.io_3_lvds_clk_p
      tlSink.bundle.io_3_lvds_data_p := io.io_3_lvds_data_p
      tlSink.bundle.io_3_lvds_data_n := io.io_3_lvds_data_n
      tlSink.bundle.io_3_lvds_valid_n := io.io_3_lvds_valid_n
      tlSink.bundle.io_3_lvds_valid_p := io.io_3_lvds_valid_p
      tlSink.bundle.io_3_lvds_frame_clk_n := io.io_3_lvds_frame_clk_n
      tlSink.bundle.io_3_lvds_frame_clk_p := io.io_3_lvds_frame_clk_p
      // CTRL 1
      tlSink.bundle.awr_host_intr1_fmc := io.awr_host_intr1_fmc
      tlSink.bundle.awr_host_intr2_fmc := io.awr_host_intr2_fmc
      tlSink.bundle.awr_host_intr3_fmc := io.awr_host_intr3_fmc
      tlSink.bundle.awr_host_intr4_fmc := io.awr_host_intr4_fmc
      io.awr_spi_cs1_fmc := tlSink.bundle.awr_spi_cs1_fmc
      io.awr_spi_cs2_fmc := tlSink.bundle.awr_spi_cs2_fmc
      io.awr_spi_cs3_fmc := tlSink.bundle.awr_spi_cs3_fmc
      io.awr_spi_cs4_fmc := tlSink.bundle.awr_spi_cs4_fmc
      tlSink.bundle.awr_spi_miso_fmc := io.awr_spi_miso_fmc
      io.awr_spi_mosi_fmc := tlSink.bundle.awr_spi_mosi_fmc
      io.awr_spi_clk_fmc := tlSink.bundle.awr_spi_clk_fmc
      io.awr_nrst1_pmod := tlSink.bundle.awr_nrst1_pmod
      io.awr_nrst2_fmc := tlSink.bundle.awr_nrst2_fmc
      // CTRL 2
      io.awr_host_intr1 := tlSink.bundle.awr_host_intr1
      io.awr_host_intr2 := tlSink.bundle.awr_host_intr2
      io.awr_host_intr3 := tlSink.bundle.awr_host_intr3
      io.awr_host_intr4 := tlSink.bundle.awr_host_intr4
      tlSink.bundle.awr_spi_cs1 := io.awr_spi_cs1
      tlSink.bundle.awr_spi_cs2 := io.awr_spi_cs2
      tlSink.bundle.awr_spi_cs3 := io.awr_spi_cs3
      tlSink.bundle.awr_spi_cs4 := io.awr_spi_cs4
      io.awr_spi_miso := tlSink.bundle.awr_spi_miso
      tlSink.bundle.awr_spi_mosi := io.awr_spi_mosi
      tlSink.bundle.awr_spi_clk := io.awr_spi_clk
      tlSink.bundle.awr_nrst1 := io.awr_nrst1
      tlSink.bundle.awr_nrst2 := io.awr_nrst2
    }
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
