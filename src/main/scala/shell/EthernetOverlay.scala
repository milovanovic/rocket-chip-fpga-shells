package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config._

case class ETHShellInput(index: Int = 0)
case class ETHDesignInput()(implicit val p: Parameters)
case class ETHOverlayOutput()
case object ETHOverlayKey extends Field[Seq[DesignPlacer[ETHDesignInput, ETHShellInput, ETHOverlayOutput]]](Nil)
trait ETHShellPlacer[Shell] extends ShellPlacer[ETHDesignInput, ETHShellInput, ETHOverlayOutput]

class ShellETHPortIO extends Bundle {
  val phy_resetn  : Bool      = Output(Bool())
  val rgmii_txd   : Vec[Bool] = Output(Vec(4, Bool()))
  val rgmii_tx_ctl: Bool      = Output(Bool())
  val rgmii_txc   : Bool      = Output(Bool())
  val rgmii_rxd   : Vec[Bool] = Input(Vec(4, Bool()))
  val rgmii_rx_ctl: Bool      = Input(Bool())
  val rgmii_rxc   : Bool      = Input(Bool())
  val mdc         : Bool      = Output(Bool())
  val mdio        : Analog    = Analog(1.W)
}

abstract class ETHPlacedOverlay(val name: String, val di: ETHDesignInput, val si: ETHShellInput)
  extends IOPlacedOverlay[ShellETHPortIO, ETHDesignInput, ETHShellInput, ETHOverlayOutput]
{
  implicit val p: Parameters = di.p

  def ioFactory = new ShellETHPortIO

  def overlayOutput: ETHOverlayOutput = ETHOverlayOutput()
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
