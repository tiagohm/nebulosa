package nebulosa.image.format

interface Hdu<T> {

    val header: ReadableHeader

    val data: T
}
