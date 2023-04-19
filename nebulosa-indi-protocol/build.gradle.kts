plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation(libs.aalto)
    implementation(libs.logback)
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
