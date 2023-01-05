package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.io.ToAttributedValueConverter

@XStreamAlias("enableBLOB")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [EnableBLOB::class])
class EnableBLOB : INDIProtocol() {

    @JvmField
    var value = BLOBEnable.ALSO

    override fun toString() = "EnableBLOB(device=$device, name=$name, value=$value, message=$message)"
}
