plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
