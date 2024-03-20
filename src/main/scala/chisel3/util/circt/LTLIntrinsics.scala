// SPDX-License-Identifier: Apache-2.0

package chisel3.util.circt

import chisel3._
import chisel3.experimental.{IntParam, IntrinsicModule, Param, StringParam}
import chisel3.experimental.hierarchy.{instantiable, public}

private object Utils {
  private[chisel3] def withoutNone(params: Map[String, Option[Param]]): Map[String, Param] =
    params.collect { case (name, Some(param)) => (name, param) }
}

@instantiable
private[chisel3] class BaseIntrinsic(
  intrinsicName: String,
  maybeParams:   Map[String, Option[Param]] = Map.empty[String, Option[Param]])
    extends IntrinsicModule(f"circt_$intrinsicName", Utils.withoutNone(maybeParams)) {
  private val paramValues = params.values.map(_ match {
    case IntParam(x)    => x.toString
    case StringParam(x) => x.toString
    case x              => x.toString
  })
  override def desiredName = paramValues.fold(this.getClass.getSimpleName) { (a, b) => a + "_" + b }
}

/** Base class for all unary intrinsics with `in` and `out` ports. */
@instantiable
private[chisel3] class UnaryLTLIntrinsic(
  intrinsicName: String,
  params:        Map[String, Option[Param]] = Map.empty[String, Option[Param]])
    extends BaseIntrinsic(f"ltl_$intrinsicName", params) {
  @public val in = IO(Input(Bool()))
  @public val out = IO(Output(Bool()))
}

/** Base class for all binary intrinsics with `lhs`, `rhs`, and `out` ports. */
@instantiable
private[chisel3] class BinaryLTLIntrinsic(
  intrinsicName: String,
  params:        Map[String, Option[Param]] = Map.empty[String, Option[Param]])
    extends BaseIntrinsic(f"ltl_$intrinsicName", params) {
  @public val lhs = IO(Input(Bool()))
  @public val rhs = IO(Input(Bool()))
  @public val out = IO(Output(Bool()))
}

/** A wrapper intrinsic for the CIRCT `ltl.and` operation. */
private[chisel3] class LTLAndIntrinsic extends BinaryLTLIntrinsic("and")

/** A wrapper intrinsic for the CIRCT `ltl.or` operation. */
private[chisel3] class LTLOrIntrinsic extends BinaryLTLIntrinsic("or")

/** A wrapper intrinsic for the CIRCT `ltl.delay` operation. */
private[chisel3] class LTLDelayIntrinsic(delay: Int, length: Option[Int])
    extends UnaryLTLIntrinsic("delay", Map("delay" -> Some(IntParam(delay)), "length" -> length.map(IntParam(_))))

/** A wrapper intrinsic for the CIRCT `ltl.concat` operation. */
private[chisel3] class LTLConcatIntrinsic extends BinaryLTLIntrinsic("concat")

/** A wrapper intrinsic for the CIRCT `ltl.not` operation. */
private[chisel3] class LTLNotIntrinsic extends UnaryLTLIntrinsic("not")

/** A wrapper intrinsic for the CIRCT `ltl.implication` operation. */
private[chisel3] class LTLImplicationIntrinsic extends BinaryLTLIntrinsic("implication")

/** A wrapper intrinsic for the CIRCT `ltl.eventually` operation. */
private[chisel3] class LTLEventuallyIntrinsic extends UnaryLTLIntrinsic("eventually")

/** A wrapper intrinsic for the CIRCT `ltl.clock` operation. */
@instantiable
private[chisel3] class LTLClockIntrinsic extends BaseIntrinsic("ltl_clock") {
  @public val in = IO(Input(Bool()))
  @public val clock = IO(Input(Clock()))
  @public val out = IO(Output(Bool()))
}

/** A wrapper intrinsic for the CIRCT `ltl.disable` operation. */
@instantiable
private[chisel3] class LTLDisableIntrinsic extends BaseIntrinsic("ltl_disable") {
  @public val in = IO(Input(Bool()))
  @public val condition = IO(Input(Bool()))
  @public val out = IO(Output(Bool()))
}

/** Base class for assert, assume, and cover intrinsics. */
@instantiable
private[chisel3] class VerifAssertLikeIntrinsic(intrinsicName: String, label: Option[String])
    extends BaseIntrinsic(f"verif_$intrinsicName", Map("label" -> label.map(StringParam(_)))) {
  @public val property = IO(Input(Bool()))
}

/** A wrapper intrinsic for the CIRCT `verif.assert` operation. */
private[chisel3] class VerifAssertIntrinsic(label: Option[String]) extends VerifAssertLikeIntrinsic("assert", label)

/** A wrapper intrinsic for the CIRCT `verif.assume` operation. */
private[chisel3] class VerifAssumeIntrinsic(label: Option[String]) extends VerifAssertLikeIntrinsic("assume", label)

/** A wrapper intrinsic for the CIRCT `verif.cover` operation. */
private[chisel3] class VerifCoverIntrinsic(label: Option[String]) extends VerifAssertLikeIntrinsic("cover", label)

// There appears to be a bug in ScalaDoc where you cannot use macro-generated methods in the same
// compilation unit as the macro-generated type. This means that any use of Definition[_] or
// Instance[_] of the above classes within this compilation unit breaks ScalaDoc generation. This
// issue appears to be similar to https://stackoverflow.com/questions/42684101 but applying the
// specific mitigation did not seem to work.  As a workaround, we simply write the extension methods
// that are generated by the @instantiable macro so that we can use them here.
private[chisel3] object LTLIntrinsicInstanceMethodsInternalWorkaround {
  import chisel3.experimental.hierarchy.Instance
  implicit val mg: chisel3.internal.MacroGenerated = new chisel3.internal.MacroGenerated {}
  implicit class UnaryLTLIntrinsicInstanceMethods(underlying: Instance[UnaryLTLIntrinsic]) {
    def in = underlying._lookup(_.in)
    def out = underlying._lookup(_.out)
  }
  implicit class BinaryLTLIntrinsicInstanceMethods(underlying: Instance[BinaryLTLIntrinsic]) {
    def lhs = underlying._lookup(_.lhs)
    def rhs = underlying._lookup(_.rhs)
    def out = underlying._lookup(_.out)
  }
  implicit class LTLClockIntrinsicInstanceMethods(underlying: Instance[LTLClockIntrinsic]) {
    def in = underlying._lookup(_.in)
    def clock = underlying._lookup(_.clock)
    def out = underlying._lookup(_.out)
  }
  implicit class LTLDisableIntrinsicInstanceMethods(underlying: Instance[LTLDisableIntrinsic]) {
    def in = underlying._lookup(_.in)
    def condition = underlying._lookup(_.condition)
    def out = underlying._lookup(_.out)
  }
  implicit class VerifAssertLikeIntrinsicInstanceMethods(underlying: Instance[VerifAssertLikeIntrinsic]) {
    def property = underlying._lookup(_.property)
  }
}
