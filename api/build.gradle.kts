import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("plugin.spring")
    kotlin("kapt")
}

dependencies {
    implementation(project(":nebulosa-astap"))
    implementation(project(":nebulosa-astrometrynet"))
    implementation(project(":nebulosa-common"))
    implementation(project(":nebulosa-guiding-phd2"))
    implementation(project(":nebulosa-hips2fits"))
    implementation(project(":nebulosa-horizons"))
    implementation(project(":nebulosa-imaging"))
    implementation(project(":nebulosa-indi-client"))
    implementation(project(":nebulosa-log"))
    implementation(project(":nebulosa-lx200-protocol"))
    implementation(project(":nebulosa-nova"))
    implementation(project(":nebulosa-sbd"))
    implementation(project(":nebulosa-simbad"))
    implementation(project(":nebulosa-stellarium-protocol"))
    implementation(project(":nebulosa-wcs"))
    implementation(libs.apache.codec)
    implementation(libs.csv)
    implementation(libs.eventbus)
    implementation(libs.flyway)
    implementation(libs.okhttp)
    implementation(libs.oshi)
    implementation(libs.rx)
    implementation(libs.sqlite)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-websocket") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.hibernate.orm:hibernate-community-dialects")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    kapt("org.springframework:spring-context-indexer:6.1.1")
    testImplementation(project(":nebulosa-test"))
}

tasks.withType<BootJar> {
    archiveFileName = "api.jar"
    destinationDirectory = file("$rootDir/desktop")

    manifest {
        attributes["Start-Class"] = "nebulosa.api.MainKt"
    }
}
