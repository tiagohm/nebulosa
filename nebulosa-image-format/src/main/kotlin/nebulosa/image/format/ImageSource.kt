package nebulosa.image.format

import nebulosa.io.SeekableSource

interface ImageSource {

    fun read(source: SeekableSource)
}
