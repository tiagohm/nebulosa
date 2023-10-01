plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.jackson)
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
