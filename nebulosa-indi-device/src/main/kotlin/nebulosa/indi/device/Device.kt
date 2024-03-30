package nebulosa.indi.device

import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.parser.INDIProtocolHandler
import java.io.Closeable

interface Device : INDIProtocolHandler, Closeable, Comparable<Device> {

    val sender: MessageSender

    val id: String

    val name: String

    val connected: Boolean

    val properties: Map<String, PropertyVector<*, *>>

    val messages: List<String>

    fun connect()

    fun disconnect()

    fun sendMessageToServer(message: INDIProtocol) {
        sender.sendMessageToServer(message)
    }

    fun ask() {
        sendMessageToServer(GetProperties().also { it.device = name })
    }

    fun enableBlob() {
        sendMessageToServer(EnableBLOB().also { it.device = name })
    }

    fun disableBlob() {
        sendMessageToServer(EnableBLOB().also { it.value = BLOBEnable.NEVER; it.device = name })
    }

    fun snoop(devices: Iterable<Device?>)

    fun sendNewSwitch(
        name: String,
        vararg elements: Pair<String, Boolean>,
    ) {
        sendNewSwitch(name, elements.asList())
    }

    fun sendNewSwitch(
        name: String,
        elements: Iterable<Pair<String, Boolean>>,
    ) {
        val vector = NewSwitchVector()
        vector.device = this.name
        vector.name = name

        for ((first, second) in elements) {
            val switch = OneSwitch()
            switch.name = first
            switch.value = second
            vector.elements.add(switch)
        }

        sendMessageToServer(vector)
    }

    fun sendNewNumber(
        name: String,
        vararg elements: Pair<String, Double>,
    ) {
        sendNewNumber(name, elements.asList())
    }

    fun sendNewNumber(
        name: String,
        elements: Iterable<Pair<String, Double>>,
    ) {
        val vector = NewNumberVector()
        vector.device = this.name
        vector.name = name

        for ((first, second) in elements) {
            val switch = OneNumber()
            switch.name = first
            switch.value = second
            vector.elements.add(switch)
        }

        sendMessageToServer(vector)
    }

    fun sendNewText(
        name: String,
        vararg elements: Pair<String, String>,
    ) {
        sendNewText(name, elements.asList())
    }

    fun sendNewText(
        name: String,
        elements: Iterable<Pair<String, String>>,
    ) {
        val vector = NewTextVector()
        vector.device = this.name
        vector.name = name

        for ((first, second) in elements) {
            val switch = OneText()
            switch.name = first
            switch.value = second
            vector.elements.add(switch)
        }

        sendMessageToServer(vector)
    }

    override fun compareTo(other: Device) = name.compareTo(other.name)
}
