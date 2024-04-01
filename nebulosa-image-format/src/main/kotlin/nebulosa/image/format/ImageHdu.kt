package nebulosa.image.format

interface ImageHdu : Hdu<ImageData> {

    val width: Int

    val height: Int

    val numberOfChannels: Int

    val isMono
        get() = numberOfChannels == 1
}
