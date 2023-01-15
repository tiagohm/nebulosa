plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":alpaca-discovery-protocol"))
    implementation(libs.okhttp)
    implementation(libs.json)
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
