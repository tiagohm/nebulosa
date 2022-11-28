package nebulosa.io

import okio.Sink

interface SeekableSink : Sink, Seekable
