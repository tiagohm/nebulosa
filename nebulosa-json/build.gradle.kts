plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.bundles.jackson)
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
