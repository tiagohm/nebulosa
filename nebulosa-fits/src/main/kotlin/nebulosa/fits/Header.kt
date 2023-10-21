package nebulosa.fits

import okio.BufferedSource
import java.util.*

class Header : FitsElement {

    private val cards = LinkedList<HeaderCard>()

    fun clear() {
        cards.clear()
    }

    operator fun contains(key: String): Boolean {
        return cards.any { it.key == key }
    }

    operator fun contains(key: FitsHeader): Boolean {
        return key.key in this
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getBoolean(key: FitsHeader, defaultValue: Boolean = false): Boolean {
        return getBoolean(key.key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getInt(key: FitsHeader, defaultValue: Int): Int {
        return getInt(key.key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getLong(key: FitsHeader, defaultValue: Long): Long {
        return getLong(key.key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getFloat(key: FitsHeader, defaultValue: Float): Float {
        return getFloat(key.key, defaultValue)
    }

    fun getDouble(key: String, defaultValue: Double): Double {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getDouble(key: FitsHeader, defaultValue: Double): Double {
        return getDouble(key.key, defaultValue)
    }

    fun getString(key: String, defaultValue: String): String {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getString(key: FitsHeader, defaultValue: String): String {
        return getString(key.key, defaultValue)
    }

    override fun read(source: BufferedSource) {
        clear()

        var count = 0

        while (!source.exhausted()) {
            val card = HeaderCard(source)

            count++

            if (cards.isEmpty()) {
                require(card.key == Standard.SIMPLE.key) { "[${card.key}] invalid keyword." }
            } else if (card.isBlank) {
                continue
            } else if (card.key == Standard.END.key) {
                break
            }

            cards.add(card)
        }

        val cardSizeInBytes = count * 80
        val numberOfBlocks = cardSizeInBytes / 2880

        if (numberOfBlocks > 0) {
            val remainingByteCount = (numberOfBlocks * 2880L) - cardSizeInBytes

            if (remainingByteCount < 0L) {
                source.skip(2880L + remainingByteCount)
            }
        }
    }
}
