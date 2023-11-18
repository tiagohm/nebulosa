plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-math"))
    api(project(":nebulosa-io"))
    api("org.apache.commons:commons-numbers-complex:1.1")
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
