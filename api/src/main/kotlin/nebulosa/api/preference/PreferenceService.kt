package nebulosa.api.preference

import com.fasterxml.jackson.databind.ObjectMapper

class PreferenceService(
    private val preferenceRepository: PreferenceRepository,
    private val objectMapper: ObjectMapper,
) {

    operator fun get(key: String) = preferenceRepository[key]

    val size
        get() = preferenceRepository.size

    fun put(entity: PreferenceEntity) {
        if (entity.key in this) preferenceRepository.update(entity)
        else preferenceRepository.add(entity)
    }

    operator fun contains(key: String) = key in preferenceRepository

    fun <T> getJSON(key: String, type: Class<out T>): T? = this[key]?.value?.let { objectMapper.readValue(it, type) }

    inline fun <reified T> getJSON(key: String) = getJSON(key, T::class.java)

    fun getBoolean(key: String) = getJSON(key, Boolean::class.java)

    fun getText(key: String) = getJSON(key, String::class.java)

    inline fun <reified T : Enum<T>> getEnum(key: String) = getText(key)?.takeIf { it.isNotBlank() }?.let { enumValueOf<T>(it) }

    fun getInt(key: String) = getJSON(key, Int::class.java)

    fun getLong(key: String) = getJSON(key, Long::class.java)

    fun getDouble(key: String) = getJSON(key, Double::class.java)

    fun putJSON(key: String, value: Any?) = put(PreferenceEntity(key, if (value == null) null else objectMapper.writeValueAsString(value)))

    fun putBoolean(key: String, value: Boolean) = putJSON(key, value)

    fun putText(key: String, value: String?) = putJSON(key, value)

    fun putEnum(key: String, value: Enum<*>) = putText(key, value.name)

    fun putInt(key: String, value: Int) = putJSON(key, value)

    fun putLong(key: String, value: Long) = putJSON(key, value)

    fun putDouble(key: String, value: Double) = putJSON(key, value)

    fun clear() = preferenceRepository.clear()

    fun delete(key: String) = preferenceRepository.delete(key)
}
