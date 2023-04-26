package nebulosa.desktop.service

import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.desktop.data.PreferenceEntity
import nebulosa.desktop.repository.app.PreferenceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PreferenceService {

    @Autowired
    private lateinit var preferenceRepository: PreferenceRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    operator fun contains(key: String): Boolean {
        return preferenceRepository.existsById(key)
    }

    operator fun get(key: String): PreferenceEntity? {
        return preferenceRepository.findByIdOrNull(key)
    }

    fun save(preference: PreferenceEntity) {
        preferenceRepository.save(preference)
    }

    fun bool(key: String, value: Boolean) {
        save(PreferenceEntity(key, "$value"))
    }

    fun bool(key: String): Boolean {
        return this[key]?.value?.toBoolean() ?: false
    }

    fun int(key: String, value: Int) {
        save(PreferenceEntity(key, "$value"))
    }

    fun int(key: String): Int? {
        return this[key]?.value?.toInt()
    }

    fun long(key: String, value: Long) {
        save(PreferenceEntity(key, "$value"))
    }

    fun long(key: String): Long? {
        return this[key]?.value?.toLong()
    }

    fun float(key: String, value: Float) {
        if (value.isFinite()) save(PreferenceEntity(key, "$value"))
    }

    fun float(key: String): Float? {
        return this[key]?.value?.toFloat()
    }

    fun double(key: String, value: Double) {
        if (value.isFinite()) save(PreferenceEntity(key, "$value"))
    }

    fun double(key: String): Double? {
        return this[key]?.value?.toDouble()
    }

    fun string(key: String, value: String?) {
        if (value != null) save(PreferenceEntity(key, value))
        else delete(key)
    }

    fun string(key: String): String? {
        return this[key]?.value
    }

    fun enum(key: String, value: Enum<*>) {
        string(key, value.name)
    }

    final inline fun <reified T : Enum<T>> enum(key: String): T? {
        return enumValueOf<T>(string(key) ?: return null)
    }

    fun json(key: String, value: Any) {
        save(PreferenceEntity(key, objectMapper.writeValueAsString(value)))
    }

    fun <T> json(key: String, type: Class<out T>): T? {
        return objectMapper.readValue(this[key]?.value ?: return null, type)
    }

    final inline fun <reified T> json(key: String): T? {
        return json(key, T::class.java)
    }

    fun delete(key: String) {
        preferenceRepository.deleteById(key)
    }

    fun clear() {
        preferenceRepository.deleteAll()
    }
}
