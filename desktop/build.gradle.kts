import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    id("maven-publish")
    // id("io.objectbox")
    id("org.springframework.boot") version "3.0.3"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.spring")
    id("org.openjfx.javafxplugin")
}

dependencies {
    implementation(project(":nebulosa-nova"))
    implementation(project(":nebulosa-indi-client"))
    implementation(project(":nebulosa-imaging"))
    implementation(project(":nebulosa-hips2fits"))
    implementation(project(":nebulosa-sbd"))
    implementation(project(":nebulosa-horizons"))
    implementation(project(":nebulosa-simbad"))
    implementation(project(":nebulosa-platesolving-astap"))
    implementation(project(":nebulosa-platesolving-astrometrynet"))
    implementation(project(":nebulosa-guiding-phd2"))
    implementation(libs.jackson)
    implementation(libs.bundles.rx)
    implementation(libs.controlsfx)
    implementation(libs.okhttp)
    implementation(libs.eventbus)
    implementation(libs.logback)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("net.kurobako:gesturefx:0.7.1")
    testImplementation(project(":nebulosa-test"))
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}

javafx {
    version = "19"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.withType<BootJar> {
    manifest {
        attributes["Start-Class"] = "nebulosa.desktop.MainKt"
    }
}
