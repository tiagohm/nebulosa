plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.openjfx.javafxplugin")
}

dependencies {
    implementation(libs.controlsfx)
    implementation(libs.logback)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}

javafx {
    version = properties["javaFX.version"]!!.toString()
    modules = listOf("javafx.controls")
}
