plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":io"))
    api(project(":nova"))
    api(project(":imaging"))
    api(project(":indi-protocol"))
    implementation(libs.logback)
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
