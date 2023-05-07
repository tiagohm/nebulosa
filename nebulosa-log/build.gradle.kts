plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.logback)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
