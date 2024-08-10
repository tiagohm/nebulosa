plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation(libs.grpc.netty)
    implementation(project(":nebulosa-grpc"))
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
