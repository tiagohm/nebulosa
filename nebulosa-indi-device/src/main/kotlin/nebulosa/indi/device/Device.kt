package nebulosa.indi.device

import nebulosa.indi.protocol.BLOBEnable
import nebulosa.indi.protocol.EnableBLOB
import nebulosa.indi.protocol.GetProperties
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NewNumberVector
import nebulosa.indi.protocol.NewSwitchVector
import nebulosa.indi.protocol.NewTextVector
import nebulosa.indi.protocol.OneNumber
import nebulosa.indi.protocol.OneSwitch
import nebulosa.indi.protocol.OneText
import nebulosa.indi.protocol.parser.INDIProtocolHandler

interface Device : INDIProtocolHandler, AutoCloseable, Comparable<Device> {

    val type: DeviceType

    val sender: MessageSender

    val id: String

    val name: String

    val connected: Boolean

    val properties: Map<String, PropertyVector<*, *>>

    val messages: List<String>

    val snoopedDevices: List<Device>

    val driver: DriverInfo

    fun connect()

    fun disconnect()

    fun sendMessageToServer(message: INDIProtocol) {
        sender.sendMessageToServer(message)
    }

    fun ask() {
        sendMessageToServer(GetProperties(name))
    }

    fun enableBlob() {
        sendMessageToServer(EnableBLOB(name))
    }

    fun disableBlob() {
        sendMessageToServer(EnableBLOB(name, value = BLOBEnable.NEVER))
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
        vector.device = if (this is CompanionDevice) main.name else this.name
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
        vector.device = if (this is CompanionDevice) main.name else this.name
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
        vector.device = if (this is CompanionDevice) main.name else this.name
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
