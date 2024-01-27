plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-erfa"))
    api(project(":nebulosa-time"))
    api(project(":nebulosa-indi-device"))
    api(project(":nebulosa-plate-solving"))
    api(project(":nebulosa-star-detection"))
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
