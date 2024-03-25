import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("plugin.spring")
    kotlin("kapt")
    id("io.objectbox")
}

dependencies {
    implementation(project(":nebulosa-alignment"))
    implementation(project(":nebulosa-astap"))
    implementation(project(":nebulosa-astrometrynet"))
    implementation(project(":nebulosa-alpaca-indi"))
    implementation(project(":nebulosa-batch-processing"))
    implementation(project(":nebulosa-common"))
    implementation(project(":nebulosa-guiding-phd2"))
    implementation(project(":nebulosa-hips2fits"))
    implementation(project(":nebulosa-horizons"))
    implementation(project(":nebulosa-image"))
    implementation(project(":nebulosa-indi-client"))
    implementation(project(":nebulosa-log"))
    implementation(project(":nebulosa-lx200-protocol"))
    implementation(project(":nebulosa-nova"))
    implementation(project(":nebulosa-sbd"))
    implementation(project(":nebulosa-simbad"))
    implementation(project(":nebulosa-stellarium-protocol"))
    implementation(project(":nebulosa-watney"))
    implementation(project(":nebulosa-wcs"))
    implementation(libs.apache.codec)
    implementation(libs.csv)
    implementation(libs.eventbus)
    implementation(libs.okhttp)
    implementation(libs.oshi)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-websocket") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    kapt("org.springframework:spring-context-indexer:6.1.4")
    testImplementation(project(":nebulosa-astrobin-api"))
    testImplementation(project(":nebulosa-skycatalog-stellarium"))
    testImplementation(project(":nebulosa-test"))
}

tasks.withType<BootJar> {
    archiveFileName = "api.jar"
    destinationDirectory = file("$rootDir/desktop")

    manifest {
        attributes["Start-Class"] = "nebulosa.api.MainKt"
    }
}

kapt {
    arguments {
        arg("objectbox.modelPath", "$projectDir/schemas/objectbox.json")
        arg("objectbox.myObjectBoxPackage", "nebulosa.api.database")
    }
}
