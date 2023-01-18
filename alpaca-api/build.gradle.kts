plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.jackson)
    implementation(libs.logback)
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
