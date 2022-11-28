plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":io"))
    api(project(":nasa"))
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
