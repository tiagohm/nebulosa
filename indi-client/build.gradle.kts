plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":io"))
    implementation(libs.xstream)
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
