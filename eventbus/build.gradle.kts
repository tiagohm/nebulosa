plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.eventbus)
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
