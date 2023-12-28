package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import devices.xilinx.xilinxnexysvideodeserializer.NexysVideoDeserializerIO
import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config._

case class LVDSShellInput(channels: Int, chips: Int)
case class LVDSDesignInput(node: BundleBridgeSource[NexysVideoDeserializerIO])(implicit val p: Parameters)
case class LVDSOverlayOutput()
case object LVDSOverlayKey extends Field[Seq[DesignPlacer[LVDSDesignInput, LVDSShellInput, LVDSOverlayOutput]]](Nil)
trait LVDSShellPlacer[Shell] extends ShellPlacer[LVDSDesignInput, LVDSShellInput, LVDSOverlayOutput]


class LVDSPortBundle(val channels: Int) extends Bundle {
  // LVDS clock, data, frame and valid
  val i_clk_p: Clock = Input(Clock())
  val i_clk_n: Clock = Input(Clock())
  val i_data_p: Vec[Bool] = Input(Vec(channels, Bool()))
  val i_data_n: Vec[Bool] = Input(Vec(channels, Bool()))
  val i_valid_p: Bool = Input(Bool())
  val i_valid_n: Bool = Input(Bool())
  val i_frame_p: Bool = Input(Bool())
  val i_frame_n: Bool = Input(Bool())
}

class ShellLVDSPortIO(val channels: Int, val chips: Int = 1) extends Bundle {
  val lvds: Vec[LVDSPortBundle] = Vec(chips, new LVDSPortBundle(channels))
}

abstract class LVDSPlacedOverlay(
                                  val name: String, val di: LVDSDesignInput, val si: LVDSShellInput)
  extends IOPlacedOverlay[ShellLVDSPortIO, LVDSDesignInput, LVDSShellInput, LVDSOverlayOutput]
{
  implicit val p: Parameters = di.p

  def ioFactory = new ShellLVDSPortIO(si.channels, si.chips)

  val lvdsSink: BundleBridgeSink[NexysVideoDeserializerIO] = sinkScope { di.node.makeSink }

  def overlayOutput: LVDSOverlayOutput = LVDSOverlayOutput()
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
