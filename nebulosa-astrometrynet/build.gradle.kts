plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-math"))
    api(project(":nebulosa-common"))
    api(project(":nebulosa-platesolver"))
    api(project(":nebulosa-retrofit"))
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
