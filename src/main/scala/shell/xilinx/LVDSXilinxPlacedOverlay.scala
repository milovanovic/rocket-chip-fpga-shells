package sifive.fpgashells.shell.xilinx

import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._

abstract class LVDSXilinxPlacedOverlay(name: String, di: LVDSDesignInput, si: LVDSShellInput, channels: Int)
  extends LVDSPlacedOverlay(name, di, si, channels)
{
  def shell: XilinxShell

  shell { InModuleBody {
    lvdsSink.bundle.i_clk_p := AnalogToUInt(io.i_clk_p).asBool.asClock
    lvdsSink.bundle.i_clk_n := AnalogToUInt(io.i_clk_n).asBool.asClock
    lvdsSink.bundle.i_valid_p := AnalogToUInt(io.i_valid_p)
    lvdsSink.bundle.i_valid_n := AnalogToUInt(io.i_valid_n)
    lvdsSink.bundle.i_frame_p := AnalogToUInt(io.i_frame_p)
    lvdsSink.bundle.i_frame_n := AnalogToUInt(io.i_frame_n)
    lvdsSink.bundle.i_data_p(0) := AnalogToUInt(io.i_data_p(0))
    lvdsSink.bundle.i_data_n(0) := AnalogToUInt(io.i_data_n(0))
    lvdsSink.bundle.i_data_p(1) := AnalogToUInt(io.i_data_p(1))
    lvdsSink.bundle.i_data_n(1) := AnalogToUInt(io.i_data_n(1))
    lvdsSink.bundle.i_data_p(2) := AnalogToUInt(io.i_data_p(2))
    lvdsSink.bundle.i_data_n(2) := AnalogToUInt(io.i_data_n(2))
    lvdsSink.bundle.i_data_p(3) := AnalogToUInt(io.i_data_p(3))
    lvdsSink.bundle.i_data_n(3) := AnalogToUInt(io.i_data_n(3))
  } }
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
