import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    id("maven-publish")
    // id("io.objectbox")
    id("org.springframework.boot") version "3.0.6"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.spring")
    kotlin("plugin.noarg")
    id("org.openjfx.javafxplugin")
    id("com.github.gmazzo.buildconfig")
}

dependencies {
    implementation(project(":nebulosa-common"))
    implementation(project(":nebulosa-guiding-internal"))
    implementation(project(":nebulosa-guiding-phd2"))
    implementation(project(":nebulosa-hips2fits"))
    implementation(project(":nebulosa-horizons"))
    implementation(project(":nebulosa-imaging"))
    implementation(project(":nebulosa-indi-client"))
    implementation(project(":nebulosa-jmetro"))
    implementation(project(":nebulosa-lx200-protocol"))
    implementation(project(":nebulosa-nova"))
    implementation(project(":nebulosa-platesolving-astap"))
    implementation(project(":nebulosa-platesolving-astrometrynet"))
    implementation(project(":nebulosa-platesolving-watney"))
    implementation(project(":nebulosa-sbd"))
    implementation(project(":nebulosa-simbad"))
    implementation(project(":nebulosa-stellarium-protocol"))
    implementation(project(":nebulosa-wcs"))
    implementation(libs.jackson)
    implementation(libs.bundles.rx)
    implementation(libs.controlsfx)
    implementation(libs.okhttp)
    implementation(libs.eventbus)
    implementation(libs.gesturefx)
    implementation(libs.charts) {
        exclude(module = "countries")
        exclude(module = "heatmap")
        exclude(group = "org.openjfx")
    }
    implementation(libs.coroutines)
    implementation(libs.coroutines.javafx)
    implementation(libs.oshi)
    implementation(libs.sqlite)
    implementation(libs.flyway)
    implementation(libs.bundles.hibernate)
    implementation(libs.logback)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation(project(":nebulosa-skycatalog-hyg"))
    testImplementation(project(":nebulosa-skycatalog-stellarium"))
    testImplementation(project(":nebulosa-test"))
    testImplementation(libs.exposed.core)
    testImplementation(libs.exposed.jdbc)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}

javafx {
    version = properties["javaFX.version"]!!.toString()
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.withType<BootJar> {
    archiveFileName.set("desktop.jar")

    manifest {
        attributes["Start-Class"] = "nebulosa.desktop.MainKt"
    }
}

buildConfig {
    packageName("nebulosa.desktop")
    useKotlinOutput()
    buildConfigField("String", "VERSION_CODE", "\"${project.properties["version.code"]}\"")
    buildConfigField("String", "VERSION_NAME", "\"${project.properties["version.name"]}\"")
}

noArg {
    invokeInitializers = true
}
