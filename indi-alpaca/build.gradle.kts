plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":alpaca-discovery-protocol"))
    api(project(":indi-client"))
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
