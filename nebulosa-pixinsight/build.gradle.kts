plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-common"))
    api(project(":nebulosa-math"))
    api(project(":nebulosa-stardetector"))
    api(project(":nebulosa-livestacker"))
    api(libs.bundles.jackson)
    api(libs.apache.codec)
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
