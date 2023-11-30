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

    fun getBoolean(key: FitsHeader, defaultValue: Boolean = false): Boolean {
        return getBoolean(key.key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getInt(key: FitsHeader, defaultValue: Int): Int {
        return getInt(key.key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getLong(key: FitsHeader, defaultValue: Long): Long {
        return getLong(key.key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getFloat(key: FitsHeader, defaultValue: Float): Float {
        return getFloat(key.key, defaultValue)
    }

    fun getDouble(key: String, defaultValue: Double): Double {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getDouble(key: FitsHeader, defaultValue: Double): Double {
        return getDouble(key.key, defaultValue)
    }

    fun getString(key: String, defaultValue: String): String {
        val card = firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getString(key: FitsHeader, defaultValue: String): String {
        return getString(key.key, defaultValue)
    }
}
