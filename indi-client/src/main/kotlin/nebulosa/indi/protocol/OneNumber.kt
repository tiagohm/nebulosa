package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import com.thoughtworks.xstream.annotations.XStreamOmitField
import nebulosa.indi.protocol.io.ToAttributedValueConverter

@XStreamAlias("oneNumber")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [OneNumber::class])
class OneNumber : OneElement<Double>(), NumberElement {

    @XStreamOmitField
    override val max = 0.0

    @XStreamOmitField
    override val min = 0.0

    override var value = 0.0

    override fun toString() = "${this::class.simpleName}(name=$name, message=$message, value=$value)"
}
