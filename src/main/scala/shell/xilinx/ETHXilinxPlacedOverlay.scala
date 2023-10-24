package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.util.Cat
import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._

abstract class ETHXilinxPlacedOverlay(name: String, di: ETHDesignInput, si: ETHShellInput)
  extends ETHPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  shell {
    InModuleBody {
      UIntToAnalog(ethSink.bundle.rgmii_tx_ctl, io.rgmii_tx_ctl, true.B)
      UIntToAnalog(ethSink.bundle.phy_resetn, io.phy_resetn, true.B)
      io.rgmii_txd.zipWithIndex.foreach{case (m, i) => UIntToAnalog(ethSink.bundle.rgmii_txd(i,i), m, true.B)}
      UIntToAnalog(ethSink.bundle.rgmii_txc, io.rgmii_txc, true.B)
      UIntToAnalog(ethSink.bundle.mdc, io.mdc, true.B)
      ethSink.bundle.rgmii_rx_ctl := AnalogToUInt(io.rgmii_rx_ctl)
      ethSink.bundle.rgmii_rxd := Cat(AnalogToUInt(io.rgmii_rxd(3)), AnalogToUInt(io.rgmii_rxd(2)), AnalogToUInt(io.rgmii_rxd(1)), AnalogToUInt(io.rgmii_rxd(0)))
      ethSink.bundle.rgmii_rxc := AnalogToUInt(io.rgmii_rxc)
//      ethSink.bundle.mdio := io.mdio
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
