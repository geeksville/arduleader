package com.geeksville.util

object PartialFunctionTools {

  /**
   * Given two partial functions, construct a partial function that calls both of them
   * whenever they are defined
   */
  def callBoth[A](p1: PartialFunction[A, Unit], p2: PartialFunction[A, Unit]) = new PartialFunction[A, Unit] {
    def isDefinedAt(x: A) = p1.isDefinedAt(x) || p2.isDefinedAt(x)

    def apply(x: A) = {
      if (p1.isDefinedAt(x))
        p1.apply(x)
      if (p2.isDefinedAt(x))
        p2.apply(x)
    }
  }
}