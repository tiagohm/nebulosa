package nebulosa.indi.protocol

class Message : INDIProtocol() {

    override fun toXML() = """<message device="$device" timestamp="$timestamp" message="$message"></message>"""
}
