/**
  * Copyright (c) 2013 Orr Sella
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package sbt.plugins.sbtstats

case class AnalyzerMetric(title: String, value: Double, total: Option[Double] = None, unit: Option[String] = None) {
  def this(title: String, value: Double, total: Double, unit: String) = this(title, value, Some(total), Some(unit))

  def this(title: String, value: Double, total: Double) = this(title, value, Some(total))

  def this(title: String, value: Double, unit: String) = this(title, value, None, Some(unit))

  override def toString = (percentage, unit) match {
    case (Some(p), Some(u)) => "%s %,.0f %s (%.1f%%)".format(title, value, u, p)
    case (Some(p), None) => "%s %,.0f (%.1f%%)".format(title, value, p)
    case (None, Some(u)) => "%s %,.0f %s".format(title, value, u)
    case _ => "%s %,.0f".format(title, value)
  }

  def percentage: Option[Double] = total match {
    case Some(t) if t != 0 => Some(value / t * 100)
    case Some(_) => Some(100)
    case _ => None
  }

  def +(that: AnalyzerMetric): AnalyzerMetric = (this.total, that.total) match {
    case (Some(t1), Some(t2)) => AnalyzerMetric(title, this.value + that.value, Some(t1 + t2), unit)
    case _ => AnalyzerMetric(title, this.value + that.value, None, unit)
  }
}