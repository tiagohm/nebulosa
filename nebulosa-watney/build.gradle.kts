plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-erfa"))
    api(project(":nebulosa-imaging"))
    api(project(":nebulosa-imaging:hfd"))
    api(project(":nebulosa-star-detection"))
    api(project(":nebulosa-plate-solving"))
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
