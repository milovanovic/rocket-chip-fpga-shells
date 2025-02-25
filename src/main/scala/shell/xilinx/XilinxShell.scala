package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import org.chipsalliance.cde.config._
import sifive.fpgashells.clocks._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.shell._

class XDC(val name: String)
{
  private var constraints: Seq[() => String] = Nil
  protected def addConstraint(command: => String) { constraints = (() => command) +: constraints }
  ElaborationArtefacts.add(name, constraints.map(_()).reverse.mkString("\n") + "\n")

  def addDiffIOStandard(io: IOPin, pin: String, standard: String, diffTerm: Boolean) {
    if (diffTerm) addConstraint(s"set_property -dict { PACKAGE_PIN ${pin} IOSTANDARD ${standard} DIFF_TERM TRUE } ${io.sdcPin}")
    else addConstraint(s"set_property -dict { PACKAGE_PIN ${pin} IOSTANDARD ${standard} DIFF_TERM FALSE } ${io.sdcPin}")
  }

  def addClockDedicatedRoute(clockPin: => String) {
    addConstraint(s"set_property CLOCK_DEDICATED_ROUTE FALSE [get_nets $clockPin ]")
  }

  def addBoardPin(io: IOPin, pin: String) {
    addConstraint(s"set_property BOARD_PIN {${pin}} ${io.sdcPin}")
  }
  def addPackagePin(io: IOPin, pin: String) {
    addConstraint(s"set_property PACKAGE_PIN {${pin}} ${io.sdcPin}")
  }
  def addIOStandard(io: IOPin, standard: String) {
    addConstraint(s"set_property IOSTANDARD {${standard}} ${io.sdcPin}")
  }
  def addPullup(io: IOPin) {
    addConstraint(s"set_property PULLUP {TRUE} ${io.sdcPin}")
  }
  def addIOB(io: IOPin) {
    if (io.isOutput) {
      addConstraint(s"set_property IOB {TRUE} [ get_cells -of_objects [ all_fanin -flat -startpoints_only ${io.sdcPin}]]")
    } else {
      addConstraint(s"set_property IOB {TRUE} [ get_cells -of_objects [ all_fanout -flat -endpoints_only ${io.sdcPin}]]")
    }
  }
  def addSlew(io: IOPin, speed: String) {
    addConstraint(s"set_property SLEW {${speed}} ${io.sdcPin}")
  }
  def addTermination(io: IOPin, kind: String) {
    addConstraint(s"set_property OFFCHIP_TERM {${kind}} ${io.sdcPin}")
  }
  def clockDedicatedRouteFalse(io: IOPin) {
    addConstraint(s"set_property CLOCK_DEDICATED_ROUTE {FALSE} [get_nets ${io.sdcPin}]")
  }
  def addDriveStrength(io: IOPin, drive: String) {
    addConstraint(s"set_property DRIVE {${drive}} ${io.sdcPin}")
  }
  def addIbufLowPower(io: IOPin, value: String) {
    addConstraint(s"set_property IBUF_LOW_PWR ${value} ${io.sdcPin}")
  }
}

abstract class XilinxShell()(implicit p: Parameters) extends IOShell
{
  val sdc = new SDC("shell.sdc")
  val xdc = new XDC("shell.xdc")
  def pllReset: ModuleValue[Bool]

  ElaborationArtefacts.add("shell.vivado.tcl",
    """set shell_vivado_tcl [file normalize [info script]]
      |set shell_vivado_idx [string last ".shell.vivado.tcl" $shell_vivado_tcl]
      |add_files -fileset [current_fileset -constrset] [string replace $shell_vivado_tcl $shell_vivado_idx 999 ".shell.sdc"]
      |add_files -fileset [current_fileset -constrset] [string replace $shell_vivado_tcl $shell_vivado_idx 999 ".shell.xdc"]
      |set extra_constr [string replace $shell_vivado_tcl $shell_vivado_idx 999 ".extra.shell.xdc"]
      |if [file exist $extra_constr] {
      |  add_files -fileset [current_fileset -constrset] [string replace $shell_vivado_tcl $shell_vivado_idx 999 ".extra.shell.xdc"]
      |}
      |""".stripMargin)

    //Including the extra .xdc file in this way is a bit of a hack since ElaborationArtefacts can't append, and this tcl will only read specific
    // files. The long term solution is to make an overlay that does nothing but include .xdc constraints
}

abstract class Series7Shell()(implicit p: Parameters) extends XilinxShell
{
  val pllFactory = new PLLFactory(this, 7, p => Module(new Series7MMCM(p)))
  override def designParameters = super.designParameters.alterPartial {
    case PLLFactoryKey => pllFactory
  }
}

abstract class UltraScaleShell()(implicit p: Parameters) extends XilinxShell
{
  val pllFactory = new PLLFactory(this, 7, p => Module(new Series7MMCM(p)))
  override def designParameters = super.designParameters.alterPartial {
    case PLLFactoryKey => pllFactory
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
