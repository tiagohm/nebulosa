package nebulosa.image.format

import nebulosa.io.SeekableSource
import okio.Sink

interface ImageFormat {

    fun read(source: SeekableSource): List<Hdu<*>>

    fun write(sink: Sink, hdus: Iterable<Hdu<*>>, modifier: ImageModifier = ImageModifier)
}
