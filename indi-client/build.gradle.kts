plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":math"))
    api(project(":imaging"))
    implementation(libs.xstream)
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
