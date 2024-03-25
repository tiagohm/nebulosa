package nebulosa.image.format

interface ImageData {

    val width: Int

    val height: Int

    val numberOfChannels: Int

    val red: FloatArray

    val green: FloatArray

    val blue: FloatArray
}
