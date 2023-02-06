plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.okio)
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
