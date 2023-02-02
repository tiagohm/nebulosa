package nebulosa.indi.protocol

class EnableBLOB : INDIProtocol() {

    @JvmField var value = BLOBEnable.ALSO

    override fun toXML() = """<enableBLOB device="$device" name="$name">$value</enableBLOB>"""
}
