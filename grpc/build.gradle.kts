import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("com.google.protobuf") version "0.9.1"
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("io.grpc:grpc-stub:1.51.1")
    api("io.grpc:grpc-protobuf:1.51.1")
    api("com.google.protobuf:protobuf-kotlin:3.21.12")
    api("io.grpc:grpc-kotlin-stub:1.3.0")
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.12"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.51.1"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.3.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}
