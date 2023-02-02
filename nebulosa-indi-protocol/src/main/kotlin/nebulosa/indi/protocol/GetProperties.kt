package nebulosa.indi.protocol

class GetProperties : INDIProtocol() {

    @JvmField val version = "1.7"

    override fun toXML() = """<getProperties version="$version" device="$device" name="$name"></getProperties>"""
}
