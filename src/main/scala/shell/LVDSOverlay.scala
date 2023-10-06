package sifive.fpgashells.shell

import chisel3._
import chisel3.experimental.Analog
import devices.xilinx.xilinxnexysvideodeserializer.NexysVideoDeserializerIO
import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config._

case class LVDSShellInput(index: Int = 0)
case class LVDSDesignInput(node: BundleBridgeSource[NexysVideoDeserializerIO])(implicit val p: Parameters)
case class LVDSOverlayOutput()
case object LVDSOverlayKey extends Field[Seq[DesignPlacer[LVDSDesignInput, LVDSShellInput, LVDSOverlayOutput]]](Nil)
trait LVDSShellPlacer[Shell] extends ShellPlacer[LVDSDesignInput, LVDSShellInput, LVDSOverlayOutput]

// Tack on cts, rts signals available on some FPGAs. They are currently unused
// by our designs.
class ShellLVDSPortIO(val channels: Int) extends Bundle {
  // LVDS clock, data, frame and valid
  val i_clk_p: Analog = Analog(1.W)
  val i_clk_n: Analog = Analog(1.W)
  val i_data_p: Vec[Analog] = Vec(channels, Analog(1.W))
  val i_data_n: Vec[Analog] = Vec(channels, Analog(1.W))
  val i_valid_p: Analog = Analog(1.W)
  val i_valid_n: Analog = Analog(1.W)
  val i_frame_p: Analog = Analog(1.W)
  val i_frame_n: Analog = Analog(1.W)
}

abstract class LVDSPlacedOverlay(
                                  val name: String, val di: LVDSDesignInput, val si: LVDSShellInput, val channels: Int)
  extends IOPlacedOverlay[ShellLVDSPortIO, LVDSDesignInput, LVDSShellInput, LVDSOverlayOutput]
{
  implicit val p: Parameters = di.p

  def ioFactory = new ShellLVDSPortIO(channels)

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
