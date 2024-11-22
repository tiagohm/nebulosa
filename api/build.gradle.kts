@file:Suppress("UNCHECKED_CAST")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonOutput
import org.apache.groovy.json.internal.JsonFastParser
import org.apache.groovy.json.internal.Value

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
    application
}

dependencies {
    implementation(project(":nebulosa-alignment"))
    implementation(project(":nebulosa-astap"))
    implementation(project(":nebulosa-astrometrynet"))
    implementation(project(":nebulosa-alpaca-indi"))
    implementation(project(":nebulosa-autofocus"))
    implementation(project(":nebulosa-curve-fitting"))
    implementation(project(":nebulosa-guiding-phd2"))
    implementation(project(":nebulosa-hips2fits"))
    implementation(project(":nebulosa-horizons"))
    implementation(project(":nebulosa-image"))
    implementation(project(":nebulosa-indi-client"))
    implementation(project(":nebulosa-job-manager"))
    implementation(project(":nebulosa-log"))
    implementation(project(":nebulosa-lx200-protocol"))
    implementation(project(":nebulosa-nova"))
    implementation(project(":nebulosa-pixinsight"))
    implementation(project(":nebulosa-sbd"))
    implementation(project(":nebulosa-simbad"))
    implementation(project(":nebulosa-siril"))
    implementation(project(":nebulosa-stellarium-protocol"))
    implementation(project(":nebulosa-util"))
    implementation(project(":nebulosa-wcs"))
    implementation(project(":nebulosa-xisf"))
    implementation(libs.apache.codec)
    implementation(libs.csv)
    implementation(libs.eventbus)
    implementation(libs.okhttp)
    implementation(libs.oshi)
    implementation(libs.koin)
    implementation(libs.airline)
    implementation(libs.h2)
    implementation(libs.bundles.exposed)
    implementation(libs.flyway)
    implementation(libs.bundles.ktor) {
        exclude(module = "ktor-http-cio-jvm")
    }
    testImplementation(project(":nebulosa-astrobin-api"))
    testImplementation(project(":nebulosa-skycatalog-stellarium"))
    testImplementation(project(":nebulosa-test"))
}

application {
    mainClass = "nebulosa.api.MainKt"
}

tasks.withType<ShadowJar> {
    isZip64 = true

    archiveFileName = "api.jar"
    destinationDirectory = file("../desktop")

    manifest {
        attributes["Main-Class"] = "nebulosa.api.MainKt"
    }
}

tasks.register("dependencyGraph") {
    doLast {
        val dependencies = project.configurations.flatMap { config ->
            config.dependencies.withType<MinimalExternalModuleDependency>().map {
                mapOf("name" to "${it.group}:${it.name}", "version" to it.version, "source" to "api")
            }
        }.toMutableSet()

        var json = File(project.rootDir, "desktop/package.json").readText()
        var desktopDependencies = JsonFastParser().parse(json) as Map<String, Value>

        with(desktopDependencies["dependencies"]!!.toValue() as Map<String, Value>) {
            for ((name, version) in this) {
                dependencies.add(mapOf("name" to name, "version" to version.stringValue(), "source" to "desktop"))
            }
        }
        with(desktopDependencies["devDependencies"]!!.toValue() as Map<String, Value>) {
            for ((name, version) in this) {
                dependencies.add(mapOf("name" to name, "version" to version.stringValue(), "source" to "desktop"))
            }
        }

        json = File(project.rootDir, "desktop/app/package.json").readText()
        desktopDependencies = JsonFastParser().parse(json) as Map<String, Value>

        with(desktopDependencies["dependencies"]!!.toValue() as Map<String, Value>) {
            for ((name, version) in this) {
                dependencies.add(mapOf("name" to name, "version" to version.stringValue(), "source" to "desktop"))
            }
        }

        json = JsonOutput.prettyPrint(JsonOutput.toJson(dependencies))
        // logger.quiet(json)
        File(project.rootDir, "desktop/src/assets/data/dependencyGraph.json").writeText(json)
    }
}
