plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-math"))
    api(project(":nebulosa-retrofit"))
    implementation(project(":nebulosa-log"))
    testImplementation(project(":nebulosa-nasa"))
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
