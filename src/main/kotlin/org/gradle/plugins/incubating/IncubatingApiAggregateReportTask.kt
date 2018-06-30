package org.gradle.plugins.incubating

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

@CacheableTask
open class IncubatingApiAggregateReportTask @Inject constructor(private val workerExecutor: WorkerExecutor) : DefaultTask() {

    @Input
    var reports: Map<String, File>? = null

    @OutputFile
    val htmlReportFile = newOutputFile().also {
        it.set(project.layout.buildDirectory.file("reports/all-incubating.html"))
    }

    @TaskAction
    fun generateReport() {
        workerExecutor.submit(GenerateReport::class.java) {
            isolationMode = IsolationMode.CLASSLOADER
            params(reports, htmlReportFile.asFile.get())
        }
    }
}

typealias ReportNameToProblems = MutableMap<String, MutableSet<String>>

open class GenerateReport @Inject constructor(private val reports: Map<String, File>, private val outputFile: File) : Runnable {
    override
    fun run() {
        val byVersion = mutableMapOf<String, ReportNameToProblems>()
        reports.forEach { name, file ->
            file.forEachLine(Charsets.UTF_8) {
                val (version, releaseDate, problem) = it.split(';')
                byVersion.getOrPut(version) {
                    mutableMapOf()
                }.getOrPut(name) {
                    mutableSetOf()
                }.add(problem)
            }
        }
        generateReport(byVersion)
    }

    private
    fun generateReport(data: Map<String, ReportNameToProblems>) {
        outputFile.parentFile.mkdirs()
        outputFile.printWriter(Charsets.UTF_8).use { writer ->
            writer.println("""<html lang="en">
    <head>
       <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
       <title>Incubating APIs</title>
       <link xmlns:xslthl="http://xslthl.sf.net" rel="stylesheet" href="https://fonts.googleapis.com/css?family=Lato:400,400i,700">
       <meta xmlns:xslthl="http://xslthl.sf.net" content="width=device-width, initial-scale=1" name="viewport">
       <link xmlns:xslthl="http://xslthl.sf.net" type="text/css" rel="stylesheet" href="https://docs.gradle.org/current/userguide/base.css">

    </head>
    <body>
       <h1>Incubating APIs</h1>
    """)
            data.toSortedMap().forEach { version, problems ->
                writer.println("<h2>Incubating since ${version}</h2>")
                problems.forEach { name, issues ->
                    writer.println("<h3>In ${name}</h3>")
                    writer.println("<ul>")
                    issues.forEach {
                        writer.println("   <li>$it</li>")
                    }
                    writer.println("</ul>")
                }

            }
            writer.println("</body></html>")
        }
    }

}