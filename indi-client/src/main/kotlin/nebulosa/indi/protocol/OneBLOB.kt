package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.io.ToAttributedValueConverter

@XStreamAlias("oneBLOB")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [OneBLOB::class])
class OneBLOB : OneElement<String>(), BLOBElement {

    @XStreamAsAttribute
    @JvmField
    var format = ""

    @XStreamAsAttribute
    @JvmField
    var size = ""

    override var value = ""

    override fun toString() = "OneBLOB(name=$name, message=$message, format=$format, size=$size)"
}

