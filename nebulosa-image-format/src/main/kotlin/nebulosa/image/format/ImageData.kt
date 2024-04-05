package nebulosa.image.format

interface ImageData {

    val width: Int

    val height: Int

    val numberOfChannels: Int

    val numberOfPixels
        get() = width * height

    val red: FloatArray

    val green: FloatArray

    val blue: FloatArray

    fun readChannelTo(channel: ImageChannel, output: FloatArray)

    companion object {

        @Suppress("NOTHING_TO_INLINE")
        inline fun Float.representableRange(rangeMin: Float, rangeMax: Float, rangeDelta: Float = rangeMax - rangeMin): Float {
            return if (this < rangeMin) 0f
            else if (this > rangeMax) 1f
            else (this - rangeMin) / rangeDelta
        }
    }
}
