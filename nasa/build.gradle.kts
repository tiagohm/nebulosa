plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":io"))
    api(project(":time"))
    implementation(libs.okhttp)
    implementation(libs.apache.codec)
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
