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
            library("xstream", "com.thoughtworks.xstream:xstream:1.4.19")
            library("okio", "com.squareup.okio:okio:3.2.0")
            library("okhttp", "com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
            library("ejml", "org.ejml:ejml-ddense:0.41.1")
            library("fits", "gov.nasa.gsfc.heasarc:nom-tam-fits:1.17.0")
            library("jackson", "com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")
            library("koin", "io.insert-koin:koin-core:3.3.0")
            library("rx-java", "io.reactivex.rxjava3:rxjava:3.1.5")
            library("kotest-assertions-core", "io.kotest:kotest-assertions-core:5.5.4")
            library("kotest-runner-junit5", "io.kotest:kotest-runner-junit5:5.5.4")
            bundle("kotest", listOf("kotest-assertions-core", "kotest-runner-junit5"))
            bundle("rx", listOf("rx-java"))
        }
    }
}

include(":constants")
include(":erfa")
include(":io")
include(":imaging")
include(":time")
include(":coordinates")
include(":nasa")
include(":math")
include(":nova")
include(":indi-client")
include(":desktop")
