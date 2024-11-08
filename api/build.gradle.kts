import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
    application
}

dependencies {
    implementation(project(":nebulosa-alignment"))
    implementation(project(":nebulosa-astap"))
    implementation(project(":nebulosa-astrometrynet"))
    implementation(project(":nebulosa-alpaca-indi"))
    implementation(project(":nebulosa-autofocus"))
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
    implementation(libs.koin)
    implementation(libs.airline)
    implementation(libs.h2)
    implementation(libs.bundles.exposed)
    implementation(libs.flyway)
    implementation(libs.bundles.ktor)
    testImplementation(project(":nebulosa-astrobin-api"))
    testImplementation(project(":nebulosa-skycatalog-stellarium"))
    testImplementation(project(":nebulosa-test"))
}

application {
    mainClass = "nebulosa.api.MainKt"
}

tasks.withType<ShadowJar> {
    isZip64 = true

    archiveFileName = "api.jar"
    destinationDirectory = file("../desktop")

    manifest {
        attributes["Main-Class"] = "nebulosa.api.MainKt"
    }
}
