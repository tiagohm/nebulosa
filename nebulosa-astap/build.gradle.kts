plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-commandline"))
    api(project(":nebulosa-platesolver"))
    api(project(":nebulosa-stardetector"))
    api(project(":nebulosa-util"))
    api(libs.csv)
    api(libs.oshi)
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
