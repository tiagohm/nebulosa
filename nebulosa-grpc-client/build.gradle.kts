plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation(project(":nebulosa-grpc"))
    implementation(libs.grpc.netty)
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
