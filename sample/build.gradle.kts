plugins {
   `java-library`
   id("org.gradle.incubating-report") version "1.0"
}

val report = tasks.create("report")
val reportZip = tasks.create("reportZip", Zip::class.java) {
   baseName = "incubating-report"
}

val allReports = mutableListOf<org.gradle.plugins.incubating.IncubatingApiReportTask>()

file("../../gradle/subprojects").list { dir, name ->
   val capitalized = name.split("-").map(String::capitalize).joinToString("")
   val srcDir = file("${dir}/$name/src/main/java")
   if (srcDir.exists()) {
      val singleReport = tasks.create("incubatingReport${capitalized}", org.gradle.plugins.incubating.IncubatingApiReportTask::class.java) {
         title.set("project $capitalized")
         versionFile.set(file("../../gradle/version.txt"))
         releasedVersionsFile.set(file("../../gradle/released-versions.json"))
         sources.set(files(srcDir))
         htmlReportFile.set(file("build/reports/incubating-${name}.html"))
         textReportFile.set(file("build/reports/incubating-${name}.txt"))
      }
      allReports.add(singleReport)
      report.dependsOn(singleReport)
      reportZip.from(singleReport.htmlReportFile)
   }
   true
}

val allReport = tasks.create("incubatingReportAll", org.gradle.plugins.incubating.IncubatingApiAggregateReportTask::class.java) {
   dependsOn(allReports)
   reports =
      allReports.associateBy({ it.title.get()}) { it.textReportFile.asFile.get() }
   htmlReportFile.set(file("build/reports/all.html"))
}
report.dependsOn(allReport)
reportZip.from(allReport.htmlReportFile)
