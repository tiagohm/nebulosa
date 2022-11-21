rootProject.name = "nebulosa"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

buildscript {
    repositories {
        mavenCentral()
    }
}

buildCache {
    local {
        directory = File(rootDir, ".cache")
        removeUnusedEntriesAfterDays = 1
    }
}
