package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.io.ToAttributedValueConverter

// TODO: Support sexagesimal format conversion.
@XStreamAlias("defNumber")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [DefNumber::class])
class DefNumber : DefElement<Double>(), NumberElement {

    override var value = 0.0

    @XStreamAsAttribute
    @JvmField
    var format = ""

    @XStreamAsAttribute
    override var max = 0.0

    @XStreamAsAttribute
    override var min = 0.0

    @XStreamAsAttribute
    @JvmField
    var step = 0.0
}
