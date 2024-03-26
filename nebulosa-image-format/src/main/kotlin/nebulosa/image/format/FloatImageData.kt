package nebulosa.image.format

@Suppress("ArrayInDataClass")
data class FloatImageData(
    override val width: Int,
    override val height: Int,
    override val numberOfChannels: Int = 1,
    override val red: FloatArray = FloatArray(width * height),
    override val green: FloatArray = if (numberOfChannels == 1) red else FloatArray(width * height),
    override val blue: FloatArray = if (numberOfChannels == 1) red else FloatArray(width * height),
) : ImageData
