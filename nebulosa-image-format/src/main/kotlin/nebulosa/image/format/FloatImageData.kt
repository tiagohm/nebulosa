package nebulosa.image.format

@Suppress("ArrayInDataClass")
data class FloatImageData(
    override val width: Int,
    override val height: Int,
    override val numberOfChannels: Int = 1,
    override val red: FloatArray = FloatArray(width * height),
    override val green: FloatArray = if (numberOfChannels == 1) red else FloatArray(width * height),
    override val blue: FloatArray = if (numberOfChannels == 1) red else FloatArray(width * height),
) : ImageData {

    override fun readChannelTo(channel: ImageChannel, output: FloatArray) {
        when (channel) {
            ImageChannel.GRAY,
            ImageChannel.RED -> red.copyInto(output)
            ImageChannel.GREEN -> green.copyInto(output)
            ImageChannel.BLUE -> blue.copyInto(output)
        }
    }
}
