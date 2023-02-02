package nebulosa.indi.protocol

class DelProperty : INDIProtocol() {

    override fun toXML() = """<delProperty device="$device" name="$name" timestamp="$timestamp" message="$message"></delProperty>"""
}
