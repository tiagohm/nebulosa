import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("io.objectbox")
    id("com.gradleup.shadow")
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

    testImplementation(project(":nebulosa-astrobin-api"))
    testImplementation(project(":nebulosa-skycatalog-stellarium"))
    testImplementation(project(":nebulosa-test"))
}

tasks.withType<ShadowJar> {
    isZip64 = true

    archiveFileName.set("api.jar")
    destinationDirectory.set(file("../desktop"))

    manifest {
        attributes["Main-Class"] = "nebulosa.api.MainKt"
    }
}

kapt {
    arguments {
        arg("objectbox.modelPath", "$projectDir/schemas/objectbox.json")
        arg("objectbox.myObjectBoxPackage", "nebulosa.api.database")
    }
}
