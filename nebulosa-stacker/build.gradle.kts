plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation(project(":nebulosa-log"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
