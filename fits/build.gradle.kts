plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":io"))
    api(libs.fits)
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
