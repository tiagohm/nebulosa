plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-common"))
    api(project(":nebulosa-math"))
    api(project(":nebulosa-plate-solving"))
    api(project(":nebulosa-star-detection"))
    api(project(":nebulosa-livestacking"))
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
