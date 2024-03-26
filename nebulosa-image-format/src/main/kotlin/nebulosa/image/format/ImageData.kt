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
}
