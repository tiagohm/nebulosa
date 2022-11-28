plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":indi-client"))
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
