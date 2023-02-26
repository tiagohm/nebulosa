package nebulosa.desktop.logic

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import java.util.*
import kotlin.io.path.createFile
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class Preferences(
    private val path: Path,
    private val objectMapper: ObjectMapper,
) {

    private val properties = Properties()

    init {
        load()
    }

    private fun load() {
        try {
            path.createFileIfNotExists().inputStream().use(properties::load)
        } catch (e: Throwable) {
            LOG.error("failed to load preferences.", e)
        }
    }

    @Synchronized
    private fun save() = path.outputStream().use { properties.store(it, null) }

    operator fun contains(key: String) = properties.containsKey(key)

    fun keys() = properties.keys.toList()

    operator fun get(key: String): String? = properties.getProperty(key)

    operator fun set(key: String, value: String?) {
        properties.setProperty(key, value)
        save()
    }

    fun bool(key: String) = this[key] == "true"

    fun bool(key: String, value: Boolean) = string(key, "$value")

    fun int(key: String) = string(key)?.toIntOrNull()

    fun int(key: String, value: Int) = string(key, "$value")

    fun long(key: String) = string(key)?.toLongOrNull()

    fun long(key: String, value: Long) = string(key, "$value")

    fun float(key: String) = string(key)?.toFloatOrNull()

    fun float(key: String, value: Float) = string(key, "$value")

    fun double(key: String) = string(key)?.toDoubleOrNull()

    fun double(key: String, value: Double) = string(key, if (value.isFinite()) "$value" else null)

    fun string(key: String) = get(key)

    fun string(key: String, value: String?) = set(key, value)

    fun bigInt(key: String) = string(key)?.let(::BigInteger)

    fun bigInt(key: String, value: BigInteger) = set(key, value.toString())

    fun bigDecimal(key: String) = string(key)?.let(::BigDecimal)

    fun bigDecimal(key: String, value: BigDecimal) = set(key, value.toString())

    inline fun <reified T : Enum<T>> enum(key: String) = enum(key, T::class.java)

    fun <T : Enum<T>> enum(key: String, type: Class<out T>): T? {
        val name = string(key)
        return type.enumConstants.firstOrNull { it.name == name }
    }

    fun <T : Enum<T>> enum(key: String, value: T) = string(key, value.name)

    inline fun <reified T> json(key: String) = json(key, T::class.java)

    fun <T> json(key: String, type: Class<out T>) = string(key)?.let { objectMapper.readValue(it, type) }

    fun json(key: String, value: Any) = string(key, objectMapper.writeValueAsString(value))

    fun delete(key: String) {
        if (properties.remove(key) != null) {
            save()
        }
    }

    fun clear() {
        if (properties.isNotEmpty()) {
            properties.clear()
            save()
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(Preferences::class.java)

        @JvmStatic
        private fun Path.createFileIfNotExists(vararg attributes: FileAttribute<*>) = try {
            createFile(*attributes)
        } catch (e: FileAlreadyExistsException) {
            this
        } catch (e: Throwable) {
            throw e
        }
    }
}
