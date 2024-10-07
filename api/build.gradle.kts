import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "3.3.3"
    kotlin("plugin.spring")
    kotlin("kapt")
    id("io.objectbox")
}

dependencies {
    implementation(project(":nebulosa-alignment"))
    implementation(project(":nebulosa-astap"))
    implementation(project(":nebulosa-astrometrynet"))
    implementation(project(":nebulosa-alpaca-indi"))
    implementation(project(":nebulosa-curve-fitting"))
    implementation(project(":nebulosa-guiding-phd2"))
    implementation(project(":nebulosa-hips2fits"))
    implementation(project(":nebulosa-horizons"))
    implementation(project(":nebulosa-image"))
    implementation(project(":nebulosa-indi-client"))
    implementation(project(":nebulosa-job-manager"))
    implementation(project(":nebulosa-log"))
    implementation(project(":nebulosa-lx200-protocol"))
    implementation(project(":nebulosa-nova"))
    implementation(project(":nebulosa-pixinsight"))
    implementation(project(":nebulosa-sbd"))
    implementation(project(":nebulosa-simbad"))
    implementation(project(":nebulosa-siril"))
    implementation(project(":nebulosa-stellarium-protocol"))
    implementation(project(":nebulosa-util"))
    implementation(project(":nebulosa-wcs"))
    implementation(project(":nebulosa-xisf"))
    implementation(libs.rx)
    implementation(libs.apache.codec)
    implementation(libs.csv)
    implementation(libs.eventbus)
    implementation(libs.okhttp)
    implementation(libs.oshi)
    implementation(libs.javalin)
    implementation(libs.validator)
    implementation(libs.koin)
    implementation(libs.airline)

    // ### REMOVER ###
    implementation("org.springframework.boot:spring-boot-starter:3.2.10")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.10") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    // ### REMOVER ###

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
