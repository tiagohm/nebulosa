package nebulosa.indi.protocol

import java.io.PrintStream

/**
 * A simple XML-like communications protocol is described for
 * interactive and automated remote control of
 * diverse instrumentation.
 *
 * @see <a href="http://www.clearskyinstitute.com/INDI/INDI.pdf">Protocol</a>
 */
sealed class INDIProtocol : HasName {

    var device = ""

    override var name = ""

    var message = ""

    var timestamp = ""

    abstract fun writeTo(stream: PrintStream)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is INDIProtocol) return false

        if (device != other.device) return false
        if (name != other.name) return false
        if (message != other.message) return false
        return timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }

    companion object {

        const val DEFAULT_PORT = 7624

        private const val QUOTE = "\""

        @JvmStatic
        internal fun PrintStream.writeXML(
            name: String,
            value: Any?,
            vararg attributes: Any?,
        ) {
            print("<")
            print(name)

            for (i in attributes.indices step 2) {
                val attrName = attributes[i]?.toString() ?: continue
                val attrValue = attributes[i + 1]?.toString() ?: continue

                if (attrName.isNotEmpty() && attrValue.isNotEmpty()) {
                    print(" ")
                    print(attrName)
                    print("=$QUOTE")
                    print(attrValue)
                    print(QUOTE)
                }
            }

            if (value != null) {
                print(">")

                when (value) {
                    is Iterable<*> -> {
                        for (message in value) {
                            if (message is INDIProtocol) message.writeTo(this)
                            else if (message != null) print(message)
                        }
                    }
                    is HasText -> print(value.text)
                    else -> print(value)
                }

                print("</")
                print(name)
                print(">")
            } else {
                print("/>")
            }
        }
    }
}
