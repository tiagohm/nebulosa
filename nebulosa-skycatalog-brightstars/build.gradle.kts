plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-skycatalog"))
    implementation(libs.logback)
    testImplementation(project(":nebulosa-simbad"))
    testImplementation(project(":nebulosa-vizier"))
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
