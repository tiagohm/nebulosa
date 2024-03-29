plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-erfa"))
    api(project(":nebulosa-imaging"))
    api(project(":nebulosa-star-detection"))
    api(project(":nebulosa-plate-solving"))
    api(libs.apache.collections)
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
