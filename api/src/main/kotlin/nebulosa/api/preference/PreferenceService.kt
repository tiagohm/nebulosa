package nebulosa.api.preference

import nebulosa.api.inject.Named
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Path
import java.util.*
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

data class PreferenceService(private val properties: Properties) : KoinComponent {

    private val defaultSavePath by inject<Path>(Named.preferencesPath)

    constructor(capacity: Int = 16) : this(Properties(capacity))

    val size
        get() = properties.size

    operator fun get(key: String): String? {
        return properties.getProperty(key)
    }

    operator fun set(key: String, value: Any) {
        properties.setProperty(key, "$value")
    }

    operator fun contains(key: String): Boolean {
        return properties.containsKey(key)
    }

    fun delete(key: String): String? {
        return properties.remove(key)?.toString()
    }

    fun clear() {
        properties.clear()
    }

    fun load(path: Path) {
        path.inputStream().use(properties::load)
    }

    @Synchronized
    fun save(path: Path? = null) {
        requireNotNull(path ?: defaultSavePath).outputStream().use { properties.store(it, "") }
    }

    companion object {

        const val FILENAME = "nebulosa.properties"
    }
}
