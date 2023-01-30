plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-io"))
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
