plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.serial)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
