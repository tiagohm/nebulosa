plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.oshi)
    api(libs.jna)
    api(project(":nebulosa-fits"))
    implementation(project(":nebulosa-log"))
    testImplementation(project(":nebulosa-io"))
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
