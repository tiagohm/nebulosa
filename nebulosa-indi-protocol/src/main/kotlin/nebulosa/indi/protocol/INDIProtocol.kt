package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import java.io.Serializable

/**
 * A simple XML-like communications protocol is described for
 * interactive and automated remote control of
 * diverse instrumentation.
 *
 * @see <a href="http://www.clearskyinstitute.com/INDI/INDI.pdf">Protocol</a>
 */
sealed class INDIProtocol : HasName, Serializable {

    @XStreamAsAttribute
    @JvmField
    var device = ""

    @XStreamAsAttribute
    override var name = ""

    @XStreamAsAttribute
    @JvmField
    var message = ""

    @XStreamAsAttribute
    @JvmField
    var timestamp = ""

    companion object {

        const val DEFAULT_PORT = 7624
    }
}
