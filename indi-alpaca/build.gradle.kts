plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":alpaca-discovery-protocol"))
    api(project(":alpaca-client"))
    api(project(":indi-protocol"))
    implementation(libs.okhttp)
    implementation(libs.jackson)
    implementation(libs.logback)
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
