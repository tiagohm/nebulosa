plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nebulosa-io"))
    api(project(":nebulosa-hips2fits"))
    api(project(":nebulosa-fits"))
    api(project(":nebulosa-xisf"))
    api(libs.okhttp)
    api(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
