plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.apache.exec)
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