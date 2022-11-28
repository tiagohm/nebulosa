package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.io.ToAttributedValueConverter

@XStreamAlias("defBLOB")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [DefBLOB::class])
class DefBLOB : DefElement<String>(), BLOBElement {

    override var value = ""
}
