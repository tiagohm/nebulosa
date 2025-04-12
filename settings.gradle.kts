rootProject.name = "nebulosa"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

buildCache {
    local {
        directory = File(rootDir, ".cache")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("okio", "com.squareup.okio:okio:3.11.0")
            library("okhttp", "com.squareup.okhttp3:okhttp:4.12.0")
            library("okhttp-logging", "com.squareup.okhttp3:logging-interceptor:4.12.0")
            library("jackson-core", "com.fasterxml.jackson.core:jackson-databind:2.18.3")
            library("jackson-jsr310", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.3")
            library("jackson-kt", "com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
            library("retrofit", "com.squareup.retrofit2:retrofit:2.11.0")
            library("retrofit-jackson", "com.squareup.retrofit2:converter-jackson:2.11.0")
            library("logback", "ch.qos.logback:logback-classic:1.5.18")
            library("eventbus", "org.greenrobot:eventbus-java:3.3.1")
            library("netty-transport", "io.netty:netty-transport:4.2.0.Final")
            library("netty-codec", "io.netty:netty-codec:4.2.0.Final")
            library("xml", "com.fasterxml:aalto-xml:1.3.3")
            library("csv", "de.siegmar:fastcsv:3.6.0")
            library("apache-lang3", "org.apache.commons:commons-lang3:3.17.0")
            library("apache-codec", "commons-codec:commons-codec:1.18.0")
            library("apache-collections", "org.apache.commons:commons-collections4:4.4")
            library("apache-math", "org.apache.commons:commons-math3:3.6.1")
            library("apache-numbers-complex", "org.apache.commons:commons-numbers-complex:1.2")
            library("oshi", "com.github.oshi:oshi-core:6.8.0")
            library("jna", "net.java.dev.jna:jna:5.17.0")
            library("koin", "io.insert-koin:koin-core:4.0.4")
            library("airline", "com.github.rvesse:airline:3.0.0")
            library("h2", "com.h2database:h2:2.3.232")
            library("flyway", "org.flywaydb:flyway-core:11.7.0")
            library("exposed-core", "org.jetbrains.exposed:exposed-core:0.60.0")
            library("exposed-jdbc", "org.jetbrains.exposed:exposed-jdbc:0.60.0")
            library("ktor-core", "io.ktor:ktor-server-core-jvm:3.0.3")
            library("ktor-websockets", "io.ktor:ktor-server-websockets:3.0.3")
            library("ktor-content-negotiation", "io.ktor:ktor-server-content-negotiation:3.0.3")
            library("ktor-jackson", "io.ktor:ktor-serialization-jackson:3.0.3")
            library("ktor-call-logging", "io.ktor:ktor-server-call-logging:3.0.3")
            library("ktor-cors", "io.ktor:ktor-server-cors:3.0.3")
            library("ktor-host-common", "io.ktor:ktor-server-host-common:3.0.3")
            library("ktor-resources", "io.ktor:ktor-server-resources:3.0.3")
            library("ktor-status-pages", "io.ktor:ktor-server-status-pages:3.0.3")
            library("ktor-netty", "io.ktor:ktor-server-netty:3.0.3")
            library("kotest", "io.kotest:kotest-assertions-core:5.9.1")
            library("junit-api", "org.junit.jupiter:junit-jupiter-api:5.12.2")
            library("junit-engine", "org.junit.jupiter:junit-jupiter-engine:5.12.2")
            bundle("netty", listOf("netty-transport", "netty-codec"))
            bundle("jackson", listOf("jackson-core", "jackson-jsr310", "jackson-kt"))
            bundle("exposed", listOf("exposed-core", "exposed-jdbc"))
            bundle("ktor", listOf("ktor-core", "ktor-websockets", "ktor-content-negotiation", "ktor-jackson", "ktor-call-logging", "ktor-cors", "ktor-host-common", "ktor-resources", "ktor-netty", "ktor-status-pages"))
        }
    }
}

include(":api")
include(":nebulosa-adql")
include(":nebulosa-alignment")
include(":nebulosa-alpaca-api")
include(":nebulosa-alpaca-discovery-protocol")
include(":nebulosa-alpaca-indi")
include(":nebulosa-astap")
include(":nebulosa-astrobin-api")
include(":nebulosa-astrometrynet")
include(":nebulosa-astrometrynet-jna")
include(":nebulosa-autofocus")
include(":nebulosa-commandline")
include(":nebulosa-constants")
include(":nebulosa-curve-fitting")
include(":nebulosa-erfa")
include(":nebulosa-fits")
include(":nebulosa-guiding")
include(":nebulosa-guiding-internal")
include(":nebulosa-guiding-phd2")
include(":nebulosa-hips2fits")
include(":nebulosa-horizons")
include(":nebulosa-image")
include(":nebulosa-image-format")
include(":nebulosa-indi-client")
include(":nebulosa-indi-device")
include(":nebulosa-indi-protocol")
include(":nebulosa-io")
include(":nebulosa-job-manager")
include(":nebulosa-json")
include(":nebulosa-livestacker")
include(":nebulosa-log")
include(":nebulosa-lx200-protocol")
include(":nebulosa-math")
include(":nebulosa-nasa")
include(":nebulosa-netty")
include(":nebulosa-nova")
include(":nebulosa-phd2-client")
include(":nebulosa-pixinsight")
include(":nebulosa-platesolver")
include(":nebulosa-retrofit")
include(":nebulosa-sbd")
include(":nebulosa-simbad")
include(":nebulosa-siril")
include(":nebulosa-skycatalog")
include(":nebulosa-skycatalog-hyg")
include(":nebulosa-skycatalog-sao")
include(":nebulosa-skycatalog-stellarium")
include(":nebulosa-stardetector")
include(":nebulosa-stacker")
include(":nebulosa-stellarium-protocol")
include(":nebulosa-test")
include(":nebulosa-time")
include(":nebulosa-util")
include(":nebulosa-vizier")
include(":nebulosa-watney")
include(":nebulosa-wcs")
include(":nebulosa-xisf")
include(":nebulosa-xml")
