plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":coordinates"))
    api(project(":erfa"))
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
