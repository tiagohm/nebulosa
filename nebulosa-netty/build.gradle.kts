plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.netty)
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
