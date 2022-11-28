package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.io.ToAttributedValueConverter

@XStreamAlias("oneNumber")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [OneNumber::class])
class OneNumber : OneElement<Double>(), NumberElement {

    @XStreamAsAttribute
    override var max = 0.0

    @XStreamAsAttribute
    override var min = 0.0

    override var value = 0.0
}
