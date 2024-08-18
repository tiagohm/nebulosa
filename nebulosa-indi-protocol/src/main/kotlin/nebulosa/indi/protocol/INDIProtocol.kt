package nebulosa.indi.protocol

import java.io.PrintStream

/**
 * A simple XML-like communications protocol is described for
 * interactive and automated remote control of
 * diverse instrumentation.
 *
 * @see <a href="http://www.clearskyinstitute.com/INDI/INDI.pdf">Protocol</a>
 */
sealed interface INDIProtocol : HasName, HasDevice, XMLOutput {

    var message: String

    var timestamp: String

    companion object {

        const val DEFAULT_PORT = 7624

        internal fun PrintStream.writeXML(
            name: String, value: Any?,
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
                    print("=\"")
                    print(attrValue)
                    print("\"")
                }
            }

            if (value != null) {
                print(">")

                when (value) {
                    is Iterable<*> -> {
                        for (message in value) {
                            if (message is XMLOutput) message.writeTo(this)
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
