package nebulosa.desktop.preferences

import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class Preferences(private val path: Path) : KoinComponent {

    private val objectMapper by inject<ObjectMapper>()

    private val data = ConcurrentHashMap<String, String>()

    init {
        load()
    }

    @Suppress("UNCHECKED_CAST")
    private fun load() {
        if (path.exists()) {
            val data = path.inputStream().use { objectMapper.readValue(it, Map::class.java) }
            this.data.putAll(data as Map<out String, String>)
        }
    }

    @Synchronized
    private fun save() {
        path.outputStream().use { objectMapper.writeValue(it, data) }
    }

    operator fun contains(key: String) = data.containsKey(key)

    fun keys() = data.keys.toList()

    fun delete(key: String) {
        data.remove(key)
        save()
    }

    fun clear() {
        data.clear()
        save()
    }

    fun bool(key: String) = data[key] == "true"

    fun bool(key: String, value: Boolean) {
        data[key] = "$value"
        save()
    }

    fun int(key: String) = data[key]?.toIntOrNull()

    fun int(key: String, value: Int) {
        data[key] = "$value"
        save()
    }

    fun long(key: String) = data[key]?.toLongOrNull()

    fun long(key: String, value: Long) {
        data[key] = "$value"
        save()
    }

    fun float(key: String) = data[key]?.toFloatOrNull()

    fun float(key: String, value: Float) {
        data[key] = "$value"
        save()
    }

    fun double(key: String) = data[key]?.toDoubleOrNull()

    fun double(key: String, value: Double) {
        data[key] = "$value"
        save()
    }

    fun string(key: String) = data[key]

    fun string(key: String, value: String) {
        data[key] = value
        save()
    }

    fun <T> json(key: String, type: Class<out T>) = data[key]?.let { objectMapper.readValue(it, type) }

    inline fun <reified T> json(key: String) = json(key, T::class.java)

    fun json(key: String, value: Any) {
        data[key] = objectMapper.writeValueAsString(value)
        save()
    }
}
