package nebulosa.image.format

interface ReadableHeader : Collection<HeaderCard> {

    operator fun contains(key: String): Boolean {
        return any { it.key == key }
    }

    operator fun contains(key: HeaderKey): Boolean {
        return key.key in this
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(Boolean::class.javaObjectType, defaultValue)
    }

    fun getBooleanOrNull(key: String): Boolean? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(Boolean::class.javaObjectType, null)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(Int::class.javaObjectType, defaultValue)
    }

    fun getIntOrNull(key: String): Int? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(Int::class.javaObjectType, null)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(Long::class.javaObjectType, defaultValue)
    }

    fun getLongOrNull(key: String): Long? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(Long::class.javaObjectType, null)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(Float::class.javaObjectType, defaultValue)
    }

    fun getFloatOrNull(key: String): Float? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(Float::class.javaObjectType, null)
    }

    fun getDouble(key: String, defaultValue: Double): Double {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(Double::class.javaObjectType, defaultValue)
    }

    fun getDoubleOrNull(key: String): Double? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(Double::class.javaObjectType, null)
    }

    fun getString(key: String, defaultValue: String): String {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(String::class.javaObjectType, defaultValue)
    }

    fun getStringOrNull(key: String): String? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(String::class.javaObjectType, null)
    }

    fun getBoolean(key: HeaderKey, defaultValue: Boolean = false): Boolean {
        return getBoolean(key.key, defaultValue)
    }

    fun getBooleanOrNull(key: HeaderKey): Boolean? {
        return getBooleanOrNull(key.key)
    }

    fun getInt(key: HeaderKey, defaultValue: Int): Int {
        return getInt(key.key, defaultValue)
    }

    fun getIntOrNull(key: HeaderKey): Int? {
        return getIntOrNull(key.key)
    }

    fun getLong(key: HeaderKey, defaultValue: Long): Long {
        return getLong(key.key, defaultValue)
    }

    fun getLongOrNull(key: HeaderKey): Long? {
        return getLongOrNull(key.key)
    }

    fun getFloat(key: HeaderKey, defaultValue: Float): Float {
        return getFloat(key.key, defaultValue)
    }

    fun getFloatOrNull(key: HeaderKey): Float? {
        return getFloatOrNull(key.key)
    }

    fun getDouble(key: HeaderKey, defaultValue: Double): Double {
        return getDouble(key.key, defaultValue)
    }

    fun getDoubleOrNull(key: HeaderKey): Double? {
        return getDoubleOrNull(key.key)
    }

    fun getString(key: HeaderKey, defaultValue: String): String {
        return getString(key.key, defaultValue)
    }

    fun getStringOrNull(key: HeaderKey): String? {
        return getStringOrNull(key.key)
    }
}
