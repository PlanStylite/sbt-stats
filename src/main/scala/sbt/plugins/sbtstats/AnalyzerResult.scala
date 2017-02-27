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

import scala.language.implicitConversions

abstract class AnalyzerResult {
  def title: String

  def metrics: Seq[AnalyzerMetric]

  override def toString = title + "\n- " + metrics.mkString("\n- ") + "\n"

  private implicit def anyToString(x: Any): String = x.toString

  def +(that: AnalyzerResult): AnalyzerResult = {
    val combinedMetrics = this.metrics ++ that.metrics
    val distinctTitles = combinedMetrics.map(_.title).distinct
    val summedMetrics = distinctTitles.map(t => combinedMetrics.filter(m => m.title == t).reduceLeft(_ + _))
    GenericAnalyzerResult(title, summedMetrics)
  }
}

case class GenericAnalyzerResult(title: String, metrics: Seq[AnalyzerMetric]) extends AnalyzerResult