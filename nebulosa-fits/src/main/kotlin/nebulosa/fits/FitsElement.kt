package nebulosa.fits

import java.nio.channels.SeekableByteChannel

interface FitsElement {

    fun read(source: SeekableByteChannel)
}
