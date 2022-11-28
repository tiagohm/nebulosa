package nebulosa.indi.protocol.io

import com.thoughtworks.xstream.core.util.CustomObjectOutputStream
import com.thoughtworks.xstream.io.StatefulWriter
import nebulosa.indi.protocol.INDIProtocol
import java.io.*
import java.util.*

internal object INDIProtocolFactory {

    private const val BUFFER_SIZE = 1 * 1024 * 1024 // 1MB

    private val CLOSE_BYTES = "</INDI>".encodeToByteArray()
    private val OPEN_BYTES = "<INDI>".encodeToByteArray()
    private val XSTREAM = INDIProtocolXStream()

    private fun inputStreamWithRootTag(input: InputStream): InputStream {
        val vector = Vector<InputStream>(3)
        vector.add(ByteArrayInputStream(OPEN_BYTES))
        vector.add(input)
        vector.add(ByteArrayInputStream(CLOSE_BYTES))
        return SequenceInputStream(vector.elements())
    }

    fun createInputStream(input: InputStream): INDIInputStream = object : INDIInputStream {
        private val buffer = BufferedInputStream(MinimalBlockingInputStream(input), BUFFER_SIZE)
        private val inputStreamWithRootTag = inputStreamWithRootTag(buffer)
        private val stream = XSTREAM.createObjectInputStream(inputStreamWithRootTag)

        override fun readINDIProtocol() = try {
            stream.readObject() as? INDIProtocol
        } catch (e: EOFException) {
            null
        } catch (e: Throwable) {
            throw IOException("deserialization failed", e)
        }

        override fun close() = input.close()
    }

    fun createOutputStream(output: OutputStream): INDIOutputStream = object : INDIOutputStream {
        private val buffer = BufferedOutputStream(output, BUFFER_SIZE)
        private val stream = CustomObjectOutputStream(object : CustomObjectOutputStream.StreamCallback {
            private val statefulWriter = StatefulWriter(XSTREAM.driver.createWriter(buffer))

            override fun close() {
                if (statefulWriter.state() != StatefulWriter.STATE_CLOSED) {
                    statefulWriter.close()
                }
            }

            override fun defaultWriteObject() = throw NotActiveException("Not in call to writeObject")

            override fun flush() = statefulWriter.flush()

            override fun writeFieldsToStream(fields: Map<*, *>) = throw NotActiveException("Not in call to writeObject")

            @Synchronized
            override fun writeToStream(o: Any) = XSTREAM.marshal(o, statefulWriter)
        })

        override fun writeINDIProtocol(message: INDIProtocol) {
            stream.writeObject(message)
            stream.flush()
        }

        override fun flush() = stream.flush()

        override fun close() = stream.close()
    }
}
