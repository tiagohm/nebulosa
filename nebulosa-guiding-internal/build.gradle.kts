plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-common"))
    api(project(":nebulosa-math"))
    api(project(":nebulosa-imaging"))
    api(project(":nebulosa-hfd"))
    api(project(":nebulosa-guiding"))
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
