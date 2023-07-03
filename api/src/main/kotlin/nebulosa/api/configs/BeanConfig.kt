package nebulosa.api.configs

import io.objectbox.BoxStore
import nebulosa.api.data.entities.MyObjectBox
import nebulosa.api.data.entities.Preference
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path
import kotlin.io.path.createDirectories

@Configuration
class BeanConfig {

    @Bean
    fun appDirectory(): Path = Path.of(System.getProperty("app.dir"))

    @Bean
    fun dataDiretory(appDirectory: Path): Path = Path.of("$appDirectory", "data").createDirectories()

    @Bean
    fun logsDiretory(appDirectory: Path): Path = Path.of("$appDirectory", "logs")

    @Bean
    fun boxStore(dataDiretory: Path) = MyObjectBox.builder()
        .baseDirectory(dataDiretory.toFile())
        .name("nebulosa")
        .build()!!

    @Bean
    fun preferenceBox(boxStore: BoxStore) = boxStore.boxFor(Preference::class.java)!!

    @Bean
    fun corsConfigurer() = object : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry
                .addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "DELETE", "PATCH")
        }
    }
}
