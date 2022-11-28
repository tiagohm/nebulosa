import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.22")
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

allprojects {
    group = "com.github.tiagohm"
    version = "0.1.0"

    repositories {
        mavenCentral()
        google()
    }

    tasks.create("downloadDependencies") {
        description = "Download all dependencies to the Gradle cache"
        doLast {
            for (configuration in configurations) {
                if (configuration.isCanBeResolved) {
                    configuration.files
                }
            }
        }
    }
}

subprojects {
    val project = this@subprojects
    if (project.name == "nebulosa-bom") return@subprojects

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.toString()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
    }

    val testJavaVersion = System.getProperty("test.java.version", "11").toInt()

    tasks.withType<Test> {
        useJUnitPlatform()

        val javaToolchains = project.extensions.getByType<JavaToolchainService>()

        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(testJavaVersion))
        })

        maxParallelForks = Runtime.getRuntime().availableProcessors() * 2

        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }

        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
