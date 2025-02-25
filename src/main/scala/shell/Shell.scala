package sifive.fpgashells.shell

import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config._

case object DesignKey extends Field[Parameters => LazyModule]

case object DesignKeyWithTestHarness extends Field[(Option[LazyScope], Parameters) => LazyModule]

// Overlays are declared by the Shell and placed somewhere by the Design
// ... they inject diplomatic code both where they were placed and in the shell
// ... they are instantiated with DesignInput and return DesignOutput
// placed overlay has been invoked by the design
trait PlacedOverlay[DesignInput, ShellInput, OverlayOutput] {
  def name: String
  def designInput: DesignInput
  def shellInput: ShellInput
  def overlayOutput: OverlayOutput
}

trait ShellPlacer[DesignInput, ShellInput, OverlayOutput] {
  def valName: ValName
  def shellInput: ShellInput
  def place(di: DesignInput): PlacedOverlay[DesignInput, ShellInput, OverlayOutput]
}

trait DesignPlacer[DesignInput, ShellInput, OverlayOutput] {
  def isPlaced: Boolean
  def name: String
  def shellInput: ShellInput
  def place(di: DesignInput): PlacedOverlay[DesignInput, ShellInput, OverlayOutput]
}

trait ShellOverlayAccessor[DesignInput, ShellInput, OverlayOutput] {
  def get(): Option[PlacedOverlay[DesignInput, ShellInput, OverlayOutput]]
}

abstract class Shell()(implicit p: Parameters) extends LazyModule with LazyScope
{
  private var overlays = Parameters.empty
  def designParameters: Parameters = overlays ++ p

  def Overlay[DesignInput, ShellInput, OverlayOutput](
      key: Field[Seq[DesignPlacer[DesignInput, ShellInput, OverlayOutput]]],
      placer: ShellPlacer[DesignInput, ShellInput, OverlayOutput]): 
    ShellOverlayAccessor[DesignInput, ShellInput, OverlayOutput] = {
    val thunk = new Object
        with ShellOverlayAccessor[DesignInput, ShellInput, OverlayOutput]
        with DesignPlacer[DesignInput, ShellInput, OverlayOutput] {
      var placedOverlay: Option[PlacedOverlay[DesignInput, ShellInput, OverlayOutput]] = None
      def get() = placedOverlay
      def isPlaced = !placedOverlay.isEmpty
      def name = placer.valName.name
      def shellInput = placer.shellInput
      def place(input: DesignInput): PlacedOverlay[DesignInput, ShellInput, OverlayOutput] = {
        require (!isPlaced, s"Overlay ${name} has already been placed by the design; cannot place again")
        val it = placer.place(input)
        placedOverlay = Some(it)
        it
      }
    }
    overlays = overlays ++ Parameters((site, here, up) => {
      case x: Field[_] if x eq key => {
        val tail = up(key)
        if (thunk.isPlaced) { tail } else { thunk +: tail }
      }
    })
    thunk
  }

  // feel free to override this if necessary
  lazy val module = new LazyRawModuleImp(this)
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
