plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.xml)
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
