package nebulosa.indi.client.io

import nebulosa.indi.parser.INDIXmlInputStream
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.io.INDIInputStream
import nebulosa.indi.protocol.io.INDIOutputStream
import nebulosa.io.MinimalBlockingInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.SequenceInputStream
import java.util.*

internal object INDIProtocolFactory {

    private val OPEN_BYTES = "<indi>".encodeToByteArray()
    private val CLOSE_BYTES = "</indi>".encodeToByteArray()

    private fun inputStreamWithRootTag(input: InputStream): InputStream {
        val vector = Vector<InputStream>(3)
        vector.add(ByteArrayInputStream(OPEN_BYTES))
        vector.add(input)
        vector.add(ByteArrayInputStream(CLOSE_BYTES))
        return SequenceInputStream(vector.elements())
    }

    fun createInputStream(input: InputStream): INDIInputStream {
        val inputStreamWithRootTag = inputStreamWithRootTag(MinimalBlockingInputStream(input))
        return INDIXmlInputStream(inputStreamWithRootTag)
    }

    fun createOutputStream(output: OutputStream): INDIOutputStream = object : INDIOutputStream {

        @Synchronized
        override fun writeINDIProtocol(message: INDIProtocol) {
            output.write(message.toXML().encodeToByteArray())
            flush()
        }

        override fun flush() = output.flush()

        override fun close() = output.close()
    }
}
