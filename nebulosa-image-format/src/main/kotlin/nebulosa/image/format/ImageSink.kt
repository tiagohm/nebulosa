package nebulosa.image.format

import okio.Sink

interface ImageSink {

    fun write(sink: Sink)
}
