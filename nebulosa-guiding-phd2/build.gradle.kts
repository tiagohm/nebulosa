plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-io"))
    api(project(":nebulosa-common"))
    api(project(":nebulosa-guiding"))
    api(project(":nebulosa-phd2-client"))
    api(project(":nebulosa-json"))
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
