import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("maven-publish")
    // id("io.objectbox")
    id("org.openjfx.javafxplugin")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":nebulosa-nova"))
    implementation(project(":nebulosa-indi-client"))
    implementation(project(":nebulosa-imaging"))
    implementation(project(":nebulosa-query-sbd"))
    implementation(project(":nebulosa-query-horizons"))
    implementation(project(":nebulosa-query-simbad"))
    implementation(libs.koin)
    implementation(libs.jackson)
    implementation(libs.bundles.rx)
    implementation(libs.controlsfx)
    implementation(libs.okhttp)
    implementation(libs.logback)
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}

javafx {
    version = "19"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("nebulosa")
        isZip64 = true
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "nebulosa.desktop.MainKt"))
        }
    }
}
