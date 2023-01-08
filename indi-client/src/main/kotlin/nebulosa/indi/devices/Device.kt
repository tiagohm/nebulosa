package nebulosa.indi.devices

import nebulosa.indi.INDIClient
import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.parser.INDIProtocolHandler
import java.io.Closeable

interface Device : INDIProtocolHandler, Closeable {

    val client: INDIClient

    val name: String

    val isConnected: Boolean

    val properties: Map<String, PropertyVector<*, *>>

    val messages: List<String>

    fun sendMessageToServer(message: INDIProtocol)

    fun connect()

    fun disconnect()

    fun ask() {
        sendMessageToServer(GetProperties().also { it.device = name })
    }

    fun enableBlob() {
        sendMessageToServer(EnableBLOB().also { it.device = name })
    }

    fun sendNewSwitch(
        name: String,
        vararg elements: Pair<String, Boolean>,
    ) {
        val vector = NewSwitchVector()
        vector.device = this.name
        vector.name = name

        for (element in elements) {
            val switch = OneSwitch()
            switch.name = element.first
            switch.value = if (element.second) SwitchState.ON else SwitchState.OFF
            vector.elements.add(switch)
        }

        sendMessageToServer(vector)
    }

    fun sendNewNumber(
        name: String,
        vararg elements: Pair<String, Double>,
    ) {
        val vector = NewNumberVector()
        vector.device = this.name
        vector.name = name

        for (element in elements) {
            val switch = OneNumber()
            switch.name = element.first
            switch.value = element.second
            vector.elements.add(switch)
        }

        sendMessageToServer(vector)
    }

    fun sendNewText(
        name: String,
        vararg elements: Pair<String, String>,
    ) {
        val vector = NewTextVector()
        vector.device = this.name
        vector.name = name

        for (element in elements) {
            val switch = OneText()
            switch.name = element.first
            switch.value = element.second
            vector.elements.add(switch)
        }

        sendMessageToServer(vector)
    }
}
