plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
