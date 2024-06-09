plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-erfa"))
    api(project(":nebulosa-image"))
    api(project(":nebulosa-stardetector"))
    api(project(":nebulosa-platesolver"))
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
