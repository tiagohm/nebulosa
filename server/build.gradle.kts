plugins {
    kotlin("jvm")
    id("maven-publish")
    // id("io.objectbox")
}

dependencies {
    implementation(project(":nova"))
    implementation(project(":indi-client"))
    implementation(project(":grpc"))
    implementation("io.grpc:grpc-netty-shaded:1.51.1")
    implementation(libs.koin)
    implementation(libs.eventbus)
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
