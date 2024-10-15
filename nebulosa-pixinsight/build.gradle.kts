plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-math"))
    api(project(":nebulosa-commandline"))
    api(project(":nebulosa-platesolver"))
    api(project(":nebulosa-stardetector"))
    api(project(":nebulosa-stacker"))
    api(project(":nebulosa-livestacker"))
    api(project(":nebulosa-json"))
    api(libs.apache.codec)
    implementation(project(":nebulosa-log"))
    testImplementation(project(":nebulosa-image"))
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
