rootProject.name = "nebulosa"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

buildCache {
    local {
        directory = File(rootDir, ".cache")
        removeUnusedEntriesAfterDays = 30
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("okio", "com.squareup.okio:okio:3.5.0")
            library("okhttp", "com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
            library("okhttp-logging", "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
            library("fits", "gov.nasa.gsfc.heasarc:nom-tam-fits:1.18.0")
            library("jackson", "com.fasterxml.jackson.core:jackson-databind:2.15.2")
            library("jackson-jsr310", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
            library("retrofit", "com.squareup.retrofit2:retrofit:2.9.0")
            library("retrofit-jackson", "com.squareup.retrofit2:converter-jackson:2.9.0")
            library("rx", "io.reactivex.rxjava3:rxjava:3.1.7")
            library("logback", "ch.qos.logback:logback-classic:1.4.11")
            library("eventbus", "org.greenrobot:eventbus-java:3.3.1")
            library("netty-transport", "io.netty:netty-transport:4.1.97.Final")
            library("netty-codec", "io.netty:netty-codec:4.1.97.Final")
            library("xml", "com.fasterxml:aalto-xml:1.3.2")
            library("csv", "de.siegmar:fastcsv:2.2.2")
            library("apache-lang3", "org.apache.commons:commons-lang3:3.13.0")
            library("apache-codec", "commons-codec:commons-codec:1.16.0")
            library("apache-collections", "org.apache.commons:commons-collections4:4.4")
            library("oshi", "com.github.oshi:oshi-core:6.4.5")
            library("timeshape", "net.iakovlev:timeshape:2022g.17")
            library("kotest-assertions-core", "io.kotest:kotest-assertions-core:5.6.2")
            library("kotest-runner-junit5", "io.kotest:kotest-runner-junit5:5.6.2")
            bundle("kotest", listOf("kotest-assertions-core", "kotest-runner-junit5"))
            bundle("netty", listOf("netty-transport", "netty-codec"))
        }
    }
}

include(":api")
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
