package sifive.fpgashells.shell.xilinx

import freechips.rocketchip.diplomacy._
import sifive.fpgashells.shell._

abstract class LVDSXilinxPlacedOverlay(name: String, di: LVDSDesignInput, si: LVDSShellInput)
  extends LVDSPlacedOverlay(name, di, si)
{
  def shell: XilinxShell

  shell { InModuleBody {
    lvdsSink.bundle.lvds.zip(io.lvds).foreach{ case (bundle, io) =>
      bundle.i_clk_p     := io.i_clk_p
      bundle.i_clk_n     := io.i_clk_n
      bundle.i_valid_p   := io.i_valid_p
      bundle.i_valid_n   := io.i_valid_n
      bundle.i_frame_p   := io.i_frame_p
      bundle.i_frame_n   := io.i_frame_n
      bundle.i_data_p(0) := io.i_data_p(0)
      bundle.i_data_n(0) := io.i_data_n(0)
      bundle.i_data_p(1) := io.i_data_p(1)
      bundle.i_data_n(1) := io.i_data_n(1)
      bundle.i_data_p(2) := io.i_data_p(2)
      bundle.i_data_n(2) := io.i_data_n(2)
      bundle.i_data_p(3) := io.i_data_p(3)
      bundle.i_data_n(3) := io.i_data_n(3)
    }
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
