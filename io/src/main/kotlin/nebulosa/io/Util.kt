@file:JvmName("IoUtil")

package nebulosa.io

@Suppress("NOTHING_TO_INLINE")
internal inline fun checkOffsetAndCount(size: Long, offset: Long, byteCount: Long) {
    if (offset or byteCount < 0L || offset > size || size - offset < byteCount) {
        throw IndexOutOfBoundsException("size=$size offset=$offset byteCount=$byteCount")
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun checkOffsetAndCount(size: Int, offset: Int, byteCount: Int) {
    if (offset or byteCount < 0 || offset > size || size - offset < byteCount) {
        throw IndexOutOfBoundsException("size=$size offset=$offset byteCount=$byteCount")
    }
}
