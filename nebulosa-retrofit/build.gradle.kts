plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.retrofit)
    api(libs.retrofit.jackson)
    api(libs.jackson)
    api(libs.jackson.jsr310)
    api(libs.okhttp)
    api(libs.okhttp.logging)
    implementation(libs.logback)
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
