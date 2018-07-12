/*
Copyright 2017-2018 EconomicSL

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
package org.economicsl.mechanisms

import cats._
import cats.implicits._

import scala.collection.immutable.TreeSet


/** Base trait representing an agent's preferences defined over a particular
  * type of `Alternative`.
  * @tparam A
  */
trait Preference[-A] {
  self =>

  /** Return an integer whose sign communicates how `a1` compares to `a2`.
    *
    * The result sign has the following meaning:
    * - negative if `a2` is preferred to `a1`.
    * - positive if `a1` is weakly preferred to `a2`.
    * - zero if indifferent between `a1` and `a2`
    */
  def compare(a1: A, a2: A): Int

  final def ordering[A1 <: A]: Ordering[A1] = {
    new Ordering[A1] {
      def compare(a1: A1, a2: A1): Int = {
        self.compare(a1, a2)
      }
    }
  }

  final def mostPreferred[A1 <: A](alternatives: Iterable[A1]) = {
    alternatives.max(ordering)
  }

  final def rank[A1 <: A](alternatives: Iterable[A1]): Map[A1, Int] = {
    val sortedAlternatives = TreeSet.empty[A1](ordering) ++ alternatives
    sortedAlternatives.zipWithIndex.aggregate(Map.empty[A1, Int])(_ + _, _ |+| _)
  }

  /** Return `a1` if `a1` is weakly preferred to `a2`; otherwise `a2`. */
  final def weaklyPrefers[A1 <: A](a1: A1, a2: A1): A1 = {
    if (compare(a1, a2) >= 0) a1 else a2
  }

}


object Preference {

  implicit val contravariant: Contravariant[Preference] = {
    new Contravariant[Preference] {
      def contramap[A, B](fa: Preference[A])(f: B => A): Preference[B] = {
        new Preference[B] {
          def compare(b1: B, b2: B): Int = {
            fa.compare(f(b1), f(b2))
          }
        }
      }
    }
  }

  /** Defines a preference for a particular alternative. */
  def particular[A](alternative: A): Preference[A] = {
    new Preference[A] {
      def compare(a1: A, a2: A): Int = {
        if ((a1 != alternative) & (a2 == alternative)) {
          -1
        } else if ((a1 == alternative) & (a2 != alternative)) {
          1
        } else {
          0
        }
      }
    }
  }

}
