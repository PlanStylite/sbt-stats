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

import sbt.Keys._
import sbt._

object StatsPlugin extends Plugin {

  lazy val statsAnalyzers = SettingKey[Seq[Analyzer]]("stats-analyzers")
  lazy val statsProject = TaskKey[Unit]("stats-project", "Prints code statistics for the current project")
  private lazy val statsProjectNoPrint = TaskKey[Seq[AnalyzerResult]](
    "stats-project-no-print", "Returns code statistics for a project, without printing it (shouldn't be used directly)")
  lazy val statsEncoding = TaskKey[String]("stats-encoding")

  override lazy val settings = Seq(
    commands += statsCommand,
    statsAnalyzers := Seq(new FilesAnalyzer(), new LinesAnalyzer(), new CharsAnalyzer()),
    statsProject := statsProjectTask(statsProjectNoPrint.value, name.value, state.value.log),
    statsProjectNoPrint := statsProjectNoPrintTask(
      statsAnalyzers.value, (sources in Compile).value, (packageBin in Compile).value, statsEncoding.value, state.value.log
    ),
    statsEncoding := scalacOptions.value.sliding(2).foldLeft("UTF-8") {
      case (_, List("-encoding", enc)) => enc
      case (enc, _) => enc
    },
    aggregate in statsProject := false,
    aggregate in statsProjectNoPrint := false
  )

  def statsCommand = Command.command("stats") { state => doCommand(state) }

  private def doCommand(state: State): State = {
    val log = state.log
    val extracted: Extracted = Project.extract(state)
    val structure = extracted.structure
    val projectRefs = structure.allProjectRefs

    val results: Seq[AnalyzerResult] = projectRefs flatMap { projectRef =>
      EvaluateTask(structure, statsProjectNoPrint, state, projectRef) match {
        case Some((_, Value(seq))) => seq
        case _ => Seq.empty
      }
    }

    val distinctTitles = results.map(_.banner).distinct
    val summedResults = distinctTitles.map(t => results.filter(r => r.banner == t).reduceLeft(_ + _))

    log.info("\nCode Statistics for project:\n")
    summedResults.foreach(res => s"${res}\n")

    state
  }

  private def statsProjectTask(results: Seq[AnalyzerResult], name: String, log: Logger) {
    log.info(s"\nCode Statistics for project '${name}':\n")
    results.foreach(res => log.info(s"${res}\n"))
  }

  private def statsProjectNoPrintTask(analyzers: Seq[Analyzer], sources: Seq[File], packageBin: File, encoding: String, log: Logger) = {
    for (a <- analyzers) yield a.analyze(sources, packageBin, encoding)
  }
}
