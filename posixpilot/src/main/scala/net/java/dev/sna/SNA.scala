/* 
 * Copyright 2010 Sanjay Dasgupta, sanjay.dasgupta@gmail.com
 * 
 * This file is part of SNA.
 *
 * SNA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * SNA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SNA.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.java.dev.sna

import com.sun.jna.Function
import scala.reflect.Manifest

class SNAF0[R](val jnaFun: Function, val manif: Manifest[R]) extends Function0[R] {
  def apply(): R =
    if (manif == Manifest.Unit) {
      jnaFun.invoke(Array[Object]()).asInstanceOf[R]
    } else {
      jnaFun.invoke(manif.erasure, Array[Object]()).asInstanceOf[R]
    }
}

class SNAF1[T, R](val jnaFun: Function, val manif: Manifest[R])
    extends Function1[T, R] {
  def apply(t1: T): R =
    if (manif == Manifest.Unit) {
      jnaFun.invoke(Array[Object](t1.asInstanceOf[Object])).asInstanceOf[R]
    } else {
      jnaFun.invoke(manif.erasure,
        Array[Object](t1.asInstanceOf[Object])).asInstanceOf[R]
    }
}

class SNAF2[T1, T2, R](val jnaFun: Function, val manif: Manifest[R])
    extends Function2[T1, T2, R] {
  def apply(t1: T1, t2: T2): R =
    if (manif == Manifest.Unit) {
      jnaFun.invoke(Array[Object](t1.asInstanceOf[Object], t2.asInstanceOf[Object])).asInstanceOf[R]
    } else {
      jnaFun.invoke(manif.erasure,
        Array[Object](t1.asInstanceOf[Object], t2.asInstanceOf[Object])).asInstanceOf[R]
    }
}

class SNAF3[T1, T2, T3, R](val jnaFun: Function, val manif: Manifest[R])
    extends Function3[T1, T2, T3, R] {
  def apply(t1: T1, t2: T2, t3: T3): R =
    if (manif == Manifest.Unit) {
      jnaFun.invoke(Array[Object](t1.asInstanceOf[Object], t2.asInstanceOf[Object], t3.asInstanceOf[Object])).asInstanceOf[R]
    } else {
      jnaFun.invoke(manif.erasure,
        Array[Object](t1.asInstanceOf[Object], t2.asInstanceOf[Object], t3.asInstanceOf[Object])).asInstanceOf[R]
    }
}

class SNAF4[T1, T2, T3, T4, R](val jnaFun: Function, val manif: Manifest[R])
    extends Function4[T1, T2, T3, T4, R] {
  def apply(t1: T1, t2: T2, t3: T3, t4: T4): R =
    if (manif == Manifest.Unit) {
      jnaFun.invoke(Array[Object](t1.asInstanceOf[Object], t2.asInstanceOf[Object], t3.asInstanceOf[Object], t4.asInstanceOf[Object])).asInstanceOf[R]
    } else {
      jnaFun.invoke(manif.erasure,
        Array[Object](t1.asInstanceOf[Object], t2.asInstanceOf[Object], t3.asInstanceOf[Object], t4.asInstanceOf[Object])).asInstanceOf[R]
    }
}

class SNAF5[T1, T2, T3, T4, T5, R](val jnaFun: Function, val manif: Manifest[R])
    extends Function5[T1, T2, T3, T4, T5, R] {
  def apply(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5): R =
    if (manif == Manifest.Unit) {
      jnaFun.invoke(Array[Object](t1.asInstanceOf[Object], t2.asInstanceOf[Object], t3.asInstanceOf[Object], t4.asInstanceOf[Object], t5.asInstanceOf[Object])).asInstanceOf[R]
    } else {
      jnaFun.invoke(manif.erasure,
        Array[Object](t1.asInstanceOf[Object], t2.asInstanceOf[Object], t3.asInstanceOf[Object], t4.asInstanceOf[Object], t5.asInstanceOf[Object])).asInstanceOf[R]
    }
}

class SNAF6[T1, T2, T3, T4, T5, T6, R](val jnaFun: Function, val manif: Manifest[R])
    extends Function6[T1, T2, T3, T4, T5, T6, R] {
  def apply(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6): R =
    if (manif == Manifest.Unit) {
      jnaFun.invoke(Array[Object](t1.asInstanceOf[Object], t2.asInstanceOf[Object], t3.asInstanceOf[Object], t4.asInstanceOf[Object], t5.asInstanceOf[Object], t6.asInstanceOf[Object])).asInstanceOf[R]
    } else {
      jnaFun.invoke(manif.erasure,
        Array[Object](t1.asInstanceOf[Object], t2.asInstanceOf[Object], t3.asInstanceOf[Object], t4.asInstanceOf[Object], t5.asInstanceOf[Object], t6.asInstanceOf[Object])).asInstanceOf[R]
    }
}

trait SNA {
  thissna =>
  val a = 0; val b = 0; val c = 0; val d = 0; val e = 0;
  val fields = thissna.getClass.getDeclaredFields.map(_.getName).mkString(":")
  val methods = thissna.getClass.getDeclaredMethods.map(_.getName).mkString(":")
  private var functions =
    if (fields contains "a:b:c:d:e")
      getClass.getDeclaredFields.filter(_.getType.getName.startsWith("net.java.dev.sna.SNAF")).map(_.getName)
    else if (fields contains "e:d:c:b:a")
      getClass.getDeclaredFields.filter(_.getType.getName.startsWith("net.java.dev.sna.SNAF")).map(_.getName).reverse
    else if (methods contains "a:b:c:d:e")
      getClass.getDeclaredMethods.filter(_.getParameterTypes.isEmpty).
        filter(_.getReturnType.getName.startsWith("net.java.dev.sna.SNAF")).map(_.getName)
    else if (methods contains "e:d:c:b:a")
      getClass.getDeclaredMethods.filter(_.getParameterTypes.isEmpty).
        filter(_.getReturnType.getName.startsWith("net.java.dev.sna.SNAF")).map(_.getName).reverse
    else
      throw new SNAException("Mapping strategy failure")

  var snaLibrary = ""

  private var nextIndex = 0

  def SNA[R](implicit manif: Manifest[R]) = {
    val i = nextIndex
    nextIndex += 1
    new SNAF0[R](Function.getFunction(snaLibrary, functions(i)), manif)
  }

  def SNA[T1, R](implicit manif: Manifest[R]) = {
    val i = nextIndex
    nextIndex += 1
    new SNAF1[T1, R](Function.getFunction(snaLibrary, functions(i)), manif)
  }

  def SNA[T1, T2, R](implicit manif: Manifest[R]) = {
    val i = nextIndex
    nextIndex += 1
    new SNAF2[T1, T2, R](Function.getFunction(snaLibrary, functions(i)), manif)
  }

  def SNA[T1, T2, T3, R](implicit manif: Manifest[R]) = {
    val i = nextIndex
    nextIndex += 1
    new SNAF3[T1, T2, T3, R](Function.getFunction(snaLibrary, functions(i)), manif)
  }

  def SNA[T1, T2, T3, T4, R](implicit manif: Manifest[R]) = {
    val i = nextIndex
    nextIndex += 1
    new SNAF4[T1, T2, T3, T4, R](Function.getFunction(snaLibrary, functions(i)), manif)
  }

  def SNA[T1, T2, T3, T4, T5, R](implicit manif: Manifest[R]) = {
    val i = nextIndex
    nextIndex += 1
    new SNAF5[T1, T2, T3, T4, T5, R](Function.getFunction(snaLibrary, functions(i)), manif)
  }

  def SNA[T1, T2, T3, T4, T5, T6, R](implicit manif: Manifest[R]) = {
    val i = nextIndex
    nextIndex += 1
    new SNAF6[T1, T2, T3, T4, T5, T6, R](Function.getFunction(snaLibrary, functions(i)), manif)
  }

}

