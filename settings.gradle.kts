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
            library("okio", "com.squareup.okio:okio:3.2.0")
            library("okhttp", "com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
            library("okhttp-logging", "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
            library("fits", "gov.nasa.gsfc.heasarc:nom-tam-fits:1.17.0")
            library("jackson", "com.fasterxml.jackson.core:jackson-databind:2.14.1")
            library("retrofit", "com.squareup.retrofit2:retrofit:2.9.0")
            library("retrofit-jackson", "com.squareup.retrofit2:converter-jackson:2.9.0")
            library("koin", "io.insert-koin:koin-core:3.3.0")
            library("rx-java", "io.reactivex.rxjava3:rxjava:3.1.5")
            library("controlsfx", "org.controlsfx:controlsfx:11.1.2")
            library("logback", "ch.qos.logback:logback-classic:1.4.5")
            library("apache-codec", "commons-codec:commons-codec:1.15")
            library("kotest-assertions-core", "io.kotest:kotest-assertions-core:5.5.4")
            library("kotest-runner-junit5", "io.kotest:kotest-runner-junit5:5.5.4")
            bundle("kotest", listOf("kotest-assertions-core", "kotest-runner-junit5"))
            bundle("rx", listOf("rx-java"))
        }
    }
}

include(":desktop")
include(":nebulosa-alpaca-api")
include(":nebulosa-alpaca-discovery-protocol")
include(":nebulosa-constants")
include(":nebulosa-erfa")
include(":nebulosa-guiding")
include(":nebulosa-guiding-local")
include(":nebulosa-guiding-phd2")
include(":nebulosa-horizons")
include(":nebulosa-imaging")
include(":nebulosa-indi-client")
include(":nebulosa-indi-connection")
include(":nebulosa-indi-device")
include(":nebulosa-indi-parser")
include(":nebulosa-indi-protocol")
include(":nebulosa-io")
include(":nebulosa-math")
include(":nebulosa-nasa")
include(":nebulosa-nova")
include(":nebulosa-platesolving")
include(":nebulosa-platesolving-astrometrynet")
include(":nebulosa-query")
include(":nebulosa-query-horizons")
include(":nebulosa-query-sbd")
include(":nebulosa-time")
include(":nebulosa-vizier")
