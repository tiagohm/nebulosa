plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-json"))
    api(libs.retrofit)
    api(libs.retrofit.jackson)
    api(libs.okhttp)
    api(libs.okhttp.logging)
    compileOnly(libs.csv)
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
