plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    compileOnly(libs.bundles.jackson)
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
