package nebulosa.indi.protocol.io

import nebulosa.indi.protocol.INDIProtocol

interface INDIInputStream : AutoCloseable {

    fun readINDIProtocol(): INDIProtocol?
}
