package nebulosa.desktop.logic

import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class Preferences(private val path: Path) : KoinComponent {

    private val objectMapper by inject<ObjectMapper>()
    private val data = HashMap<String, String?>()

    init {
        load()
    }

    @Suppress("UNCHECKED_CAST")
    private fun load() {
        if (path.exists()) {
            val data = path.inputStream().use { objectMapper.readValue(it, Map::class.java) }
            this.data.putAll(data as Map<out String, String?>)
        }
    }

    @Synchronized
    private fun save() = path.outputStream().use { objectMapper.writeValue(it, data) }

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

    fun bool(key: String): Boolean {
        return data[key] == "true"
    }

    fun bool(key: String, value: Boolean) {
        data[key] = "$value"
        save()
    }

    fun int(key: String): Int? {
        return data[key]?.toIntOrNull()
    }

    fun int(key: String, value: Int) {
        data[key] = "$value"
        save()
    }

    fun long(key: String): Long? {
        return data[key]?.toLongOrNull()
    }

    fun long(key: String, value: Long) {
        data[key] = "$value"
        save()
    }

    fun float(key: String): Float? {
        return data[key]?.toFloatOrNull()
    }

    fun float(key: String, value: Float) {
        data[key] = "$value"
        save()
    }

    fun double(key: String): Double? {
        return data[key]?.toDoubleOrNull()
    }

    fun double(key: String, value: Double) {
        data[key] = "$value"
        save()
    }

    fun string(key: String): String? {
        return data[key]
    }

    fun string(key: String, value: String?) {
        data[key] = value
        save()
    }

    inline fun <reified T : Enum<T>> enum(key: String) = enum(key, T::class.java)

    fun <T : Enum<T>> enum(key: String, type: Class<out T>): T? {
        return type.enumConstants.firstOrNull { it.name == data[key] }
    }

    fun enum(key: String, value: Enum<*>) {
        data[key] = value.name
        save()
    }

    inline fun <reified T> json(key: String) = json(key, T::class.java)

    fun <T> json(key: String, type: Class<out T>): T? {
        return data[key]?.let { objectMapper.readValue(it, type) }
    }

    fun json(key: String, value: Any) {
        data[key] = objectMapper.writeValueAsString(value)
        save()
    }
}
