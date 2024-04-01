package nebulosa.image.format

sealed interface Hdu<out T> {

    val header: ReadableHeader

    val data: T
}
