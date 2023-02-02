package nebulosa.indi.protocol

import java.io.Serializable

/**
 * A simple XML-like communications protocol is described for
 * interactive and automated remote control of
 * diverse instrumentation.
 *
 * @see <a href="http://www.clearskyinstitute.com/INDI/INDI.pdf">Protocol</a>
 */
sealed class INDIProtocol : HasName, Serializable {

    @JvmField var device = ""

    override var name = ""

    @JvmField var message = ""

    @JvmField var timestamp = ""

    abstract fun toXML(): String

    override fun toString() = toXML()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is INDIProtocol) return false

        if (device != other.device) return false
        if (name != other.name) return false
        if (message != other.message) return false
        if (timestamp != other.timestamp) return false

        return true
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

        @Suppress("NOTHING_TO_INLINE")
        internal inline fun <E : INDIProtocol> List<E>.toXML() = joinToString("") { it.toXML() }
    }
}
