package nebulosa.astrometrynet.plate.solving

import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder

@FieldOrder("tan", "aOrder", "bOrder", "a", "b", "apOrder", "bpOrder", "ap", "bp")
sealed class Sip : Structure() {

    @JvmField var tan = Tan.ByValue()
    @JvmField var aOrder = 0
    @JvmField var bOrder = 0
    @JvmField val a = DoubleArray(SIP_MAXORDER * SIP_MAXORDER)
    @JvmField val b = DoubleArray(SIP_MAXORDER * SIP_MAXORDER)
    @JvmField var apOrder = 0
    @JvmField var bpOrder = 0
    @JvmField val ap = DoubleArray(SIP_MAXORDER * SIP_MAXORDER)
    @JvmField val bp = DoubleArray(SIP_MAXORDER * SIP_MAXORDER)

    class ByReference : Sip(), Structure.ByReference

    class ByValue : Sip(), Structure.ByValue

    companion object {

        const val SIP_MAXORDER = 10
    }
}
