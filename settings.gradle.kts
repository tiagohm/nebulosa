rootProject.name = "nebulosa"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

buildCache {
    local {
        directory = File(rootDir, ".cache")
        removeUnusedEntriesAfterDays = 1
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("okio", "com.squareup.okio:okio:3.3.0")
            library("okhttp", "com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
            library("okhttp-logging", "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
            library("fits", "gov.nasa.gsfc.heasarc:nom-tam-fits:1.17.1")
            library("jackson", "com.fasterxml.jackson.core:jackson-databind:2.15.2")
            library("jackson-jsr310", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
            library("retrofit", "com.squareup.retrofit2:retrofit:2.9.0")
            library("retrofit-jackson", "com.squareup.retrofit2:converter-jackson:2.9.0")
            library("rx-java", "io.reactivex.rxjava3:rxjava:3.1.6")
            library("controlsfx", "org.controlsfx:controlsfx:11.1.2")
            library("logback", "ch.qos.logback:logback-classic:1.4.8")
            library("eventbus", "org.greenrobot:eventbus-java:3.3.1")
            library("charts", "eu.hansolo.fx:charts:17.1.35")
            library("gesturefx", "net.kurobako:gesturefx:0.7.1")
            library("netty-transport", "io.netty:netty-transport:4.1.93.Final")
            library("netty-codec", "io.netty:netty-codec:4.1.93.Final")
            library("aalto", "com.fasterxml:aalto-xml:1.3.2")
            library("csv", "de.siegmar:fastcsv:2.2.2")
            library("coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
            library("coroutines-javafx", "org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.7.1")
            library("apache-lang3", "org.apache.commons:commons-lang3:3.12.0")
            library("oshi", "com.github.oshi:oshi-core:6.4.3")
            library("exposed-core", "org.jetbrains.exposed:exposed-core:0.41.1")
            library("exposed-jdbc", "org.jetbrains.exposed:exposed-jdbc:0.41.1")
            library("sqlite", "org.xerial:sqlite-jdbc:3.42.0.0")
            library("hibernate-core", "org.hibernate.orm:hibernate-core:6.2.4.Final")
            library("hibernate-dialects", "org.hibernate.orm:hibernate-community-dialects:6.2.4.Final")
            library("flyway", "org.flywaydb:flyway-core:9.19.4")
            library("kotest-assertions-core", "io.kotest:kotest-assertions-core:5.6.2")
            library("kotest-runner-junit5", "io.kotest:kotest-runner-junit5:5.6.2")
            bundle("kotest", listOf("kotest-assertions-core", "kotest-runner-junit5"))
            bundle("rx", listOf("rx-java"))
            bundle("netty", listOf("netty-transport", "netty-codec"))
            bundle("hibernate", listOf("hibernate-core", "hibernate-dialects"))
        }
    }
}

include(":desktop")
include(":nebulosa-adql")
include(":nebulosa-alignment")
include(":nebulosa-alpaca-api")
include(":nebulosa-alpaca-discovery-protocol")
include(":nebulosa-astrometrynet-nova")
include(":nebulosa-common")
include(":nebulosa-constants")
include(":nebulosa-erfa")
include(":nebulosa-fits")
include(":nebulosa-guiding")
include(":nebulosa-guiding-internal")
include(":nebulosa-guiding-phd2")
include(":nebulosa-hips2fits")
include(":nebulosa-horizons")
include(":nebulosa-imaging")
include(":nebulosa-indi-client")
include(":nebulosa-indi-device")
include(":nebulosa-indi-protocol")
include(":nebulosa-io")
include(":nebulosa-log")
include(":nebulosa-jmetro")
include(":nebulosa-lx200-protocol")
include(":nebulosa-math")
include(":nebulosa-nasa")
include(":nebulosa-netty")
include(":nebulosa-nova")
include(":nebulosa-phd2-client")
include(":nebulosa-platesolving")
include(":nebulosa-platesolving-astap")
include(":nebulosa-platesolving-astrometrynet")
include(":nebulosa-platesolving-watney")
include(":nebulosa-projection")
include(":nebulosa-retrofit")
include(":nebulosa-sbd")
include(":nebulosa-simbad")
include(":nebulosa-skycatalog")
include(":nebulosa-skycatalog-hyg")
include(":nebulosa-skycatalog-sao")
include(":nebulosa-skycatalog-stellarium")
include(":nebulosa-stellarium-protocol")
include(":nebulosa-test")
include(":nebulosa-time")
include(":nebulosa-vizier")
include(":nebulosa-wcs")
