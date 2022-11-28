package nebulosa.indi.protocol.io

import nebulosa.indi.protocol.INDIProtocol
import java.io.Closeable

interface INDIInputStream : Closeable {

    fun readINDIProtocol(): INDIProtocol?
}
