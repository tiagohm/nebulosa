package nebulosa.api.indi

import nebulosa.api.validators.Validatable
import nebulosa.api.validators.notEmpty
import nebulosa.api.validators.positive
import nebulosa.api.validators.range
import nebulosa.indi.protocol.INDIProtocol

data class INDIServerStart(
    @JvmField val port: Int = INDIProtocol.DEFAULT_PORT,
    @JvmField val executables: Collection<String> = emptyList<String>(),
    @JvmField val verbose: Boolean = false,
    @JvmField val restarts: Int = 10,
) : Validatable {

    override fun validate() {
        port.range(1, 65535)
        executables.notEmpty()
        restarts.positive()
    }

    companion object {

        val EMPTY = INDIServerStart()
    }
}
