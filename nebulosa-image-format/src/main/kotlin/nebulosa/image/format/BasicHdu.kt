package nebulosa.image.format

data class BasicHdu<T>(override val header: ReadableHeader, override val data: T) : Hdu<T>
