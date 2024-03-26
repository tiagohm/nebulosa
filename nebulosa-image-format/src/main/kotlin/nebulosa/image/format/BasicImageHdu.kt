package nebulosa.image.format

data class BasicImageHdu(
    override val width: Int,
    override val height: Int,
    override val numberOfChannels: Int,
    override val header: ReadableHeader,
    override val data: ImageData,
) : ImageHdu
