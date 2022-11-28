plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":constants"))
    api(libs.ejml)
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
