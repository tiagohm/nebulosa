package nebulosa.astrometrynet.plate.solving

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder

@FieldOrder(
    "parity", "table", "antype", "xname", "yname", "xunits", "yunits",
    "xtype", "ytype", "includeFlux", "includeBackground",
)
sealed class XYList : Structure() {

    @JvmField var parity = 0
    @JvmField var table: Pointer? = null
    @JvmField var antype: String? = null
    @JvmField var xname: String? = null
    @JvmField var yname: String? = null
    @JvmField var xunits: String? = null
    @JvmField var yunits: String? = null
    @JvmField var xtype = 0
    @JvmField var ytype = 0
    @JvmField var includeFlux = 0.toByte()
    @JvmField var includeBackground = 0.toByte()

    class ByReference : XYList(), Structure.ByReference

    class ByValue : XYList(), Structure.ByValue

    override fun toString(): String {
        return "XYList(parity=$parity, table=$table, antype=$antype, xname=$xname, " +
                "yname=$yname, xunits=$xunits, yunits=$yunits, xtype=$xtype, ytype=$ytype, " +
                "includeFlux=$includeFlux, includeBackground=$includeBackground)"
    }
}
