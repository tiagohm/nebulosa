package nebulosa.desktop.preferences

import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class Preferences(private val path: Path) : KoinComponent {

    private val objectMapper by inject<ObjectMapper>()

    private val data = HashMap<String, String?>()
    private val publisher = BehaviorSubject.create<Unit>()

    init {
        load()

        publisher.debounce(1L, TimeUnit.SECONDS).subscribe { save() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun load() {
        if (path.exists()) {
            val data = path.inputStream().use { objectMapper.readValue(it, Map::class.java) }
            this.data.putAll(data as Map<out String, String?>)
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
        publisher.onNext(Unit)
    }

    fun int(key: String) = data[key]?.toIntOrNull()

    fun int(key: String, value: Int) {
        data[key] = "$value"
        publisher.onNext(Unit)
    }

    fun long(key: String) = data[key]?.toLongOrNull()

    fun long(key: String, value: Long) {
        data[key] = "$value"
        publisher.onNext(Unit)
    }

    fun float(key: String) = data[key]?.toFloatOrNull()

    fun float(key: String, value: Float) {
        data[key] = "$value"
        publisher.onNext(Unit)
    }

    fun double(key: String) = data[key]?.toDoubleOrNull()

    fun double(key: String, value: Double) {
        data[key] = "$value"
        publisher.onNext(Unit)
    }

    fun string(key: String) = data[key]

    fun string(key: String, value: String?) {
        data[key] = value
        publisher.onNext(Unit)
    }

    fun <T : Enum<T>> enum(key: String, type: Class<out T>) = type.enumConstants.firstOrNull { it.name == data[key] }

    inline fun <reified T : Enum<T>> enum(key: String) = enum(key, T::class.java)

    fun enum(key: String, value: Enum<*>) {
        data[key] = value.name
        publisher.onNext(Unit)
    }

    fun <T> json(key: String, type: Class<out T>) = data[key]?.let { objectMapper.readValue(it, type) }

    inline fun <reified T> json(key: String) = json(key, T::class.java)

    fun json(key: String, value: Any) {
        data[key] = objectMapper.writeValueAsString(value)
        publisher.onNext(Unit)
    }
}
