plugins {
    kotlin("jvm")
    id("maven-publish")
    id("io.objectbox")
    id("org.openjfx.javafxplugin")
}

dependencies {
    implementation(project(":indi-client"))
    implementation(project(":imaging"))
    implementation(libs.eventbus)
    implementation(libs.koin)
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
