plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group = "org.gradle.plugins.incubating"
version = "1.0"

gradlePlugin {
    (plugins) {
        "incubating" {
            id = "org.gradle.incubating-report"
            implementationClass = "org.gradle.plugins.incubating.IncubatingApiReportPlugin"
        }
    }
}

publishing {
    repositories {
        maven(url = "build/repository")
    }
}

repositories {
    jcenter()
}

dependencies {
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.6.11")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val list = listOf("foo", "bar")
