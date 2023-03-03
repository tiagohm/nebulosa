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
            library("fits", "gov.nasa.gsfc.heasarc:nom-tam-fits:1.17.0")
            library("jackson", "com.fasterxml.jackson.core:jackson-databind:2.14.2")
            library("jackson-jsr310", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
            library("retrofit", "com.squareup.retrofit2:retrofit:2.9.0")
            library("retrofit-jackson", "com.squareup.retrofit2:converter-jackson:2.9.0")
            library("rx-java", "io.reactivex.rxjava3:rxjava:3.1.6")
            library("controlsfx", "org.controlsfx:controlsfx:11.1.2")
            library("logback", "ch.qos.logback:logback-classic:1.4.5")
            library("eventbus", "org.greenrobot:eventbus-java:3.3.1")
            library("charts", "eu.hansolo.fx:charts:17.1.27")
            library("netty", "io.netty:netty-all:4.1.89.Final")
            library("kotest-assertions-core", "io.kotest:kotest-assertions-core:5.5.5")
            library("kotest-runner-junit5", "io.kotest:kotest-runner-junit5:5.5.5")
            bundle("kotest", listOf("kotest-assertions-core", "kotest-runner-junit5"))
            bundle("rx", listOf("rx-java"))
        }
    }
}

include(":desktop")
include(":nebulosa-alignment")
include(":nebulosa-alpaca-api")
include(":nebulosa-alpaca-discovery-protocol")
include(":nebulosa-astrometrynet-nova")
include(":nebulosa-constants")
include(":nebulosa-erfa")
include(":nebulosa-fits")
include(":nebulosa-guiding")
include(":nebulosa-guiding-local")
include(":nebulosa-guiding-phd2")
include(":nebulosa-hips2fits")
include(":nebulosa-horizons")
include(":nebulosa-imaging")
include(":nebulosa-indi-client")
include(":nebulosa-indi-connection")
include(":nebulosa-indi-device")
include(":nebulosa-indi-parser")
include(":nebulosa-indi-protocol")
include(":nebulosa-io")
include(":nebulosa-lx200-protocol")
include(":nebulosa-math")
include(":nebulosa-nasa")
include(":nebulosa-netty")
include(":nebulosa-nova")
include(":nebulosa-phd2-client")
include(":nebulosa-platesolving")
include(":nebulosa-platesolving-astap")
include(":nebulosa-platesolving-astrometrynet")
include(":nebulosa-projection")
include(":nebulosa-retrofit")
include(":nebulosa-sbd")
include(":nebulosa-simbad")
include(":nebulosa-stellarium-protocol")
include(":nebulosa-test")
include(":nebulosa-time")
include(":nebulosa-vizier")
