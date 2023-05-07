plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation(libs.aalto)
    implementation(project(":nebulosa-log"))
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
