plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-io"))
    api(project(":nebulosa-nova"))
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
