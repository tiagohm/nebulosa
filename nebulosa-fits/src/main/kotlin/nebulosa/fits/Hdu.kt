package nebulosa.fits

import kotlin.math.max

interface Hdu<T> : FitsElement, Collection<T> {

    val header: Header

    val data: Array<out T>

    override val size
        get() = data.size

    override fun contains(element: T): Boolean {
        return data.any { it === element }
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { it in this }
    }

    override fun isEmpty(): Boolean {
        return data.isEmpty()
    }

    override fun iterator(): Iterator<T> {
        return data.iterator()
    }

    companion object {

        const val BLOCK_SIZE = 2880

        @JvmStatic
        fun computeRemainingBytesToSkip(sizeInBytes: Long): Long {
            val numberOfBlocks = (sizeInBytes / BLOCK_SIZE) + 1
            val remainingByteCount = (numberOfBlocks * BLOCK_SIZE) - sizeInBytes
            return max(0L, remainingByteCount)
        }
    }
}
