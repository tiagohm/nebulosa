plugins {
    kotlin("jvm")
    id("maven-publish")
    // id("io.objectbox")
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.spring")
    id("org.openjfx.javafxplugin")
}

dependencies {
    implementation(project(":nebulosa-nova"))
    implementation(project(":nebulosa-indi-client"))
    implementation(project(":nebulosa-imaging"))
    implementation(project(":nebulosa-query-sbd"))
    implementation(project(":nebulosa-query-horizons"))
    implementation(project(":nebulosa-query-simbad"))
    implementation(libs.jackson)
    implementation(libs.bundles.rx)
    implementation(libs.controlsfx)
    implementation(libs.okhttp)
    implementation(libs.logback)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
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
