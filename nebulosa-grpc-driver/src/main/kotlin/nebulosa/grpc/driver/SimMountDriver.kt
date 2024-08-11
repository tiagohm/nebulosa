package nebulosa.grpc.driver

import nebulosa.grpc.Element

class SimMountDriver : AbstractDriver(), MountDriver {

    override val name = "Mount Simulator"

    override val slewing: Boolean
        get() = TODO("Not yet implemented")

    override val connected: Boolean
        get() = TODO("Not yet implemented")

    override fun handleTextProperty(name: String, elements: List<Element>) {
        TODO("Not yet implemented")
    }

    override fun handleNumberProperty(name: String, elements: List<Element>) {
        TODO("Not yet implemented")
    }

    override fun handleSwitchProperty(name: String, elements: List<Element>) {
        TODO("Not yet implemented")
    }
}
