package nebulosa.api

import io.objectbox.Box
import io.objectbox.BoxStore
import nebulosa.api.cameras.CameraCaptureHistory
import nebulosa.api.cameras.MyObjectBox
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories

@SpringBootApplication
class Nebulosa {

    @Bean
    fun appDirectory(): Path {
        val userHomeDir = Paths.get(System.getProperty("user.home"))
        // TODO: Use different directory name based on current OS.
        val appDirectory = Paths.get("$userHomeDir", ".nebula")
        appDirectory.createDirectories()
        return appDirectory
    }

    @Bean
    fun boxStore(appDirectory: Path): BoxStore {
        return MyObjectBox.builder()
            .baseDirectory(appDirectory.toFile())
            .name("nebulosa")
            .build()
    }

    @Bean
    fun boxForCameraCaptureHistory(boxStore: BoxStore): Box<CameraCaptureHistory> {
        return boxStore.boxFor(CameraCaptureHistory::class.java)
    }

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry
                    .addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "DELETE", "PATCH")
            }
        }
    }
}
