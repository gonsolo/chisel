// SPDX-License-Identifier: Apache-2.0

package chiselTests.util

import chisel3.util.BitPat
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class BitPatSpec extends AnyFlatSpec with Matchers {
  behavior of classOf[BitPat].toString

  it should "convert a BitPat to readable form" in {
    val testPattern = "0" * 32 + "1" * 32 + "?" * 32 + "?01" * 32
    BitPat("b" + testPattern).toString should be (s"BitPat($testPattern)")
  }

  it should "convert a BitPat to raw form" in {
    val testPattern = "0" * 32 + "1" * 32 + "?" * 32 + "?01" * 32
    BitPat("b" + testPattern).rawString should be(testPattern)
  }

  it should "not fail if BitPat width is 0" in {
    intercept[IllegalArgumentException]{BitPat("b")}
  }

  it should "contact BitPat via ##" in {
    (BitPat.Y(4) ## BitPat.dontCare(3) ## BitPat.N(2)).toString should be (s"BitPat(1111???00)")
  }
}