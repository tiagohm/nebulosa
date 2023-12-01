package nebulosa.fits

interface ReadableHeader : Collection<HeaderCard> {

    operator fun contains(key: String): Boolean {
        return any { it.key == key }
    }

    operator fun contains(key: FitsHeader): Boolean {
        return key.key in this
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getBooleanOrNull(key: String): Boolean? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(null)
    }

    fun getBoolean(key: FitsHeader, defaultValue: Boolean = false): Boolean {
        return getBoolean(key.key, defaultValue)
    }

    fun getBooleanOrNull(key: FitsHeader): Boolean? {
        return getBooleanOrNull(key.key)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getIntOrNull(key: String): Int? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(null)
    }

    fun getInt(key: FitsHeader, defaultValue: Int): Int {
        return getInt(key.key, defaultValue)
    }

    fun getIntOrNull(key: FitsHeader): Int? {
        return getIntOrNull(key.key)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getLongOrNull(key: String): Long? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(null)
    }

    fun getLong(key: FitsHeader, defaultValue: Long): Long {
        return getLong(key.key, defaultValue)
    }

    fun getLongOrNull(key: FitsHeader): Long? {
        return getLongOrNull(key.key)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getFloatOrNull(key: String): Float? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(null)
    }

    fun getFloat(key: FitsHeader, defaultValue: Float): Float {
        return getFloat(key.key, defaultValue)
    }

    fun getFloatOrNull(key: FitsHeader): Float? {
        return getFloatOrNull(key.key)
    }

    fun getDouble(key: String, defaultValue: Double): Double {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getDoubleOrNull(key: String): Double? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(null)
    }

    fun getDouble(key: FitsHeader, defaultValue: Double): Double {
        return getDouble(key.key, defaultValue)
    }

    fun getDoubleOrNull(key: FitsHeader): Double? {
        return getDoubleOrNull(key.key)
    }

    fun getString(key: String, defaultValue: String): String {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getStringOrNull(key: String): String? {
        val card = firstOrNull { it.key == key } ?: return null
        return card.getValue(null)
    }

    fun getString(key: FitsHeader, defaultValue: String): String {
        return getString(key.key, defaultValue)
    }

    fun getStringOrNull(key: FitsHeader): String? {
        return getStringOrNull(key.key)
    }
}
