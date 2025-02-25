package sifive.fpgashells.clocks

import chisel3._
import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config._
import sifive.fpgashells.shell._

case class PLLNode(val feedback: Boolean)(implicit valName: ValName)
  extends MixedNexusNode(ClockImp, ClockGroupImp)(
    dFn = { _ => ClockGroupSourceParameters() },
    uFn = { _ => ClockSinkParameters() })

case class PLLInClockParameters(
  freqMHz:  Double,
  jitter:   Double = 50,
  feedback: Boolean = false)

case class PLLOutClockParameters(
  freqMHz:       Double,
  phaseDeg:      Double = 0,
  dutyCycle:     Double = 50, // in percent
  // used to create constraints:
  jitterPS:      Double = 300,
  freqErrorPPM:  Double = 10000,
  phaseErrorDeg: Double = 5)

case class PLLParameters( 
  name:  String,
  input: PLLInClockParameters,
  req:   Seq[PLLOutClockParameters])

trait PLLInstance {
  def getInput: Clock
  def getReset: Option[Bool]
  def getLocked: Bool
  def getClocks: Seq[Clock]
  def getClockNames: Seq[String]
}

case object PLLFactoryKey extends Field[PLLFactory]
class PLLFactory(scope: IOShell, maxOutputs: Int, gen: PLLParameters => PLLInstance)
{
  private var pllNodes: Seq[PLLNode] = Nil

  def apply(feedback: Boolean = false)(implicit valName: ValName, p: Parameters): PLLNode = {
    val node = scope { PLLNode(feedback) }
    pllNodes = node +: pllNodes
    node
  }

  val plls: ModuleValue[Seq[(PLLInstance, PLLNode)]] = scope { InModuleBody {
    val plls = pllNodes.flatMap { case node =>
      require (node.in.size == 1)
      val (in, edgeIn) = node.in(0)
      val (out, edgeOut) = node.out.unzip

      val params = PLLParameters(
        name = node.valName.name,
        input = PLLInClockParameters(
          freqMHz  = edgeIn.clock.freqMHz,
          jitter   = edgeIn.source.jitterPS.getOrElse(50),
          feedback = node.feedback),
        req = edgeOut.flatMap(_.members).map { e =>
          PLLOutClockParameters(
            freqMHz       = e.clock.freqMHz,
            phaseDeg      = e.sink.phaseDeg,
            dutyCycle     = e.clock.dutyCycle,
            jitterPS      = e.sink.jitterPS,
            freqErrorPPM  = e.sink.freqErrorPPM,
            phaseErrorDeg = e.sink.phaseErrorDeg)})

      val pll = gen(params)
      pll.getInput := in.clock
      pll.getReset.foreach { _ := in.reset }
      (out.flatMap(_.member) zip pll.getClocks) foreach { case (o, i) =>
        o.clock := i
        o.reset := !pll.getLocked || in.reset
      }
      Some((pll, node))
    }

    // Require all clock group names to be distinct
    val sdcGroups = Map() ++ plls.flatMap { case tuple =>
    val (pll, node) = tuple
    val (out, edgeOut) = node.out.unzip
      val groupLabels = edgeOut.flatMap(e => Seq.fill(e.members.size) { e.sink.name })
      groupLabels zip pll.getClocks.map(x => IOPin(x))
    }.groupBy(_._1).mapValues(_.map(_._2))

    // Ensure there are no clock groups with the same name
    require (sdcGroups.size == pllNodes.map(_.edges.out.size).sum)
    sdcGroups.foreach { case (_, clockPins) => scope.sdc.addGroup(pins = clockPins) }

    plls
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
