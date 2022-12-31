import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("io.objectbox")
    id("org.openjfx.javafxplugin")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":indi-client"))
    implementation(project(":imaging"))
    implementation(libs.koin)
    implementation(libs.bundles.rx)
    testImplementation(libs.bundles.kotest)
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
