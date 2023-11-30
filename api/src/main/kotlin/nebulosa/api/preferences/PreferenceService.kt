package nebulosa.api.preferences

import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.api.image.PlateSolverType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class PreferenceService(
    private val preferenceRepository: PreferenceRepository,
    private val objectMapper: ObjectMapper,
) {

    fun keys() = preferenceRepository.findAll().map { it.key }

    operator fun get(key: String) = preferenceRepository.findByIdOrNull(key)

    @Synchronized
    fun put(entity: PreferenceEntity): Unit = run { preferenceRepository.saveAndFlush(entity) }

    operator fun contains(key: String) = preferenceRepository.existsById(key)

    fun <T> getJSON(key: String, type: Class<out T>): T? = this[key]?.value?.let { objectMapper.readValue(it, type) }

    final inline fun <reified T> getJSON(key: String) = getJSON(key, T::class.java)

    fun getBoolean(key: String) = getJSON(key, Boolean::class.java)

    fun getText(key: String) = getJSON(key, String::class.java)

    final inline fun <reified T : Enum<T>> getEnum(key: String) = getText(key)?.takeIf { it.isNotBlank() }?.let { enumValueOf<T>(it) }

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

    @Synchronized
    fun clear() = preferenceRepository.deleteAll()

    @Synchronized
    fun delete(key: String) = preferenceRepository.deleteById(key)

    final inline var astapPath
        get() = getJSON<Path>(ASTAP_PATH)
        set(value) = putJSON(ASTAP_PATH, value)

    final inline var astrometryNetPath
        get() = getJSON<Path>(ASTROMETRY_NEY_PATH)
        set(value) = putJSON(ASTROMETRY_NEY_PATH, value)

    final inline var plateSolverType
        get() = getEnum<PlateSolverType>(PLATE_SOLVER_TYPE) ?: PlateSolverType.ASTAP
        set(value) = putEnum(PLATE_SOLVER_TYPE, value)

    final inline var skyAtlasVersion
        get() = getText(SKY_ATLAS_VERSION)
        set(value) = putText(SKY_ATLAS_VERSION, value)

    final inline var satellitesUpdatedAt
        get() = getLong(SATELLITES_UPDATED_AT) ?: 0L
        set(value) = putLong(SATELLITES_UPDATED_AT, value)

    companion object {

        const val ASTAP_PATH = "ASTAP_PATH"
        const val ASTROMETRY_NEY_PATH = "ASTROMETRY_NEY_PATH"
        const val PLATE_SOLVER_TYPE = "PLATE_SOLVER_TYPE"
        const val SKY_ATLAS_VERSION = "SKY_ATLAS_VERSION"
        const val SATELLITES_UPDATED_AT = "SATELLITES_UPDATED_AT"
    }
}
