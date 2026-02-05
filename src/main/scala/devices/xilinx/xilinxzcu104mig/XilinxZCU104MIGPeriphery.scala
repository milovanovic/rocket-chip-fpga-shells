package sifive.fpgashells.devices.xilinx.xilinxzcu104mig

import chisel3._
import org.chipsalliance.cde.config._
import freechips.rocketchip.subsystem.{BaseSubsystem, MBUS}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, AddressRange}
import freechips.rocketchip.tilelink.{TLWidthWidget}
case object MemoryXilinxDDRKey extends Field[XilinxZCU104MIGParams]

trait HasMemoryXilinxZCU104MIG { this: BaseSubsystem =>
  val module: HasMemoryXilinxZCU104MIGModuleImp

  val xilinxzcu104mig = LazyModule(new XilinxZCU104MIG(p(MemoryXilinxDDRKey)))

  private val mbus = locateTLBusWrapper(MBUS)
  mbus.coupleTo("xilinxzcu104mig") { xilinxzcu104mig.node := TLWidthWidget(mbus.beatBytes) := _ }
}

trait HasMemoryXilinxZCU104MIGBundle {
  val xilinxzcu104mig: XilinxZCU104MIGIO
  def connectXilinxZCU104MIGToPads(pads: XilinxZCU104MIGPads): Unit = {
    pads <> xilinxzcu104mig
  }
}

trait HasMemoryXilinxZCU104MIGModuleImp extends LazyModuleImp
    with HasMemoryXilinxZCU104MIGBundle {
  val outer: HasMemoryXilinxZCU104MIG
  val ranges = AddressRange.fromSets(p(MemoryXilinxDDRKey).address)
  require (ranges.size == 1, "DDR range must be contiguous")
  val depth = ranges.head.size
  val xilinxzcu104mig = IO(new XilinxZCU104MIGIO(depth))

  xilinxzcu104mig <> outer.xilinxzcu104mig.module.io.port
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
