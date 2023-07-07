import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "3.1.1"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.spring")
    kotlin("kapt")
    id("io.objectbox")
}

dependencies {
    implementation(project(":nebulosa-common"))
    implementation(project(":nebulosa-guiding-internal"))
    implementation(project(":nebulosa-guiding-phd2"))
    implementation(project(":nebulosa-hips2fits"))
    implementation(project(":nebulosa-horizons"))
    implementation(project(":nebulosa-imaging"))
    implementation(project(":nebulosa-indi-client"))
    implementation(project(":nebulosa-lx200-protocol"))
    implementation(project(":nebulosa-nova"))
    implementation(project(":nebulosa-platesolving-astap"))
    implementation(project(":nebulosa-platesolving-astrometrynet"))
    implementation(project(":nebulosa-platesolving-watney"))
    implementation(project(":nebulosa-sbd"))
    implementation(project(":nebulosa-simbad"))
    implementation(project(":nebulosa-stellarium-protocol"))
    implementation(project(":nebulosa-wcs"))
    implementation(project(":nebulosa-log"))
    implementation(libs.csv)
    implementation(libs.jackson)
    implementation(libs.okhttp)
    implementation(libs.oshi)
    implementation(libs.apache.codec)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-validation")
    kapt("org.springframework:spring-context-indexer:6.0.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation(project(":nebulosa-skycatalog-hyg"))
    testImplementation(project(":nebulosa-skycatalog-stellarium"))
    testImplementation(project(":nebulosa-test"))
}

tasks.withType<BootJar> {
    archiveFileName.set("api.jar")

    manifest {
        attributes["Start-Class"] = "nebulosa.api.MainKt"
    }
}
