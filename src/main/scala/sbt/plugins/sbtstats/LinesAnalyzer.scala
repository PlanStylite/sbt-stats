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

import java.io.File

import scala.io.Source

class LinesAnalyzer extends Analyzer {
  def analyze(sources: Seq[File], packageBin: File, encoding: String) = {
    val lines: Seq[Line] = for {
      file <- sources
      line <- Source.fromFile(file, encoding).getLines
    } yield new Line(line)

    val totalLines = lines.length
    val codeLines = lines.count(_.isCode)
    val commentLines = lines.count(_.isComment)
    val bracketLines = lines.count(_.isBracket)
    val blankLines = lines.count(_.isBlank)
    //val avgLength = lines.filter(_.isCode).map(_.length).foldLeft(0)(_ + _) / totalLines // check that totalLines is not 0

    new LinesAnalyzerResult(totalLines, codeLines, commentLines, bracketLines, blankLines)
  }
}

class LinesAnalyzerResult(
                           totalLines: Int,
                           codeLines: Int,
                           commentLines: Int,
                           bracketLines: Int,
                           blankLines: Int)
  extends AnalyzerResult {

  val banner = "Lines"
  val metrics =
    Seq(
      new AnalyzerMetric("Total:     ", totalLines, "lines"),
      new AnalyzerMetric("Code:      ", codeLines, totalLines, "lines"),
      new AnalyzerMetric("Comment:   ", commentLines, totalLines, "lines"),
      new AnalyzerMetric("Blank:     ", blankLines, totalLines, "lines"),
      new AnalyzerMetric("Bracket:   ", bracketLines, totalLines, "lines"))
  // new AnalyzerMetric("Avg length:", avgLength, "characters (code lines only, not inc spaces)"))
}
