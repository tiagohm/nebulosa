plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-common"))
    api(project(":nebulosa-math"))
    api(project(":nebulosa-livestacker"))
    implementation(project(":nebulosa-log"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
