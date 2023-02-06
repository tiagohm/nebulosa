plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-imaging"))
    api(project(":nebulosa-nova"))
    api(project(":nebulosa-indi-parser"))
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
