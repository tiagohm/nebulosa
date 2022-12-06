plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.springframework.boot") version "3.0.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.spring") version "1.7.22"
    id("io.objectbox")
}

dependencies {
    implementation(project(":indi-client"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
