package nebulosa.api.indi

import nebulosa.commandline.CommandLine
import nebulosa.commandline.CommandLineHandler
import nebulosa.commandline.CommandLineListener
import nebulosa.indi.device.Device
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.protocol.PropertyType
import nebulosa.log.loggerFor
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.math.max
import kotlin.math.min

class INDIService(
    private val indiEventHandler: INDIEventHandler,
    private val executorService: ExecutorService,
) : CommandLineListener {

    private val serverHandler = CommandLineHandler()
    private val serverLogs = LinkedList<String>()

    init {
        serverHandler.registerCommandLineListener(this)
    }

    fun registerDeviceToSendMessage(device: Device) {
        indiEventHandler.registerDevice(device)
    }

    fun unregisterDeviceToSendMessage(device: Device) {
        indiEventHandler.unregisterDevice(device)
    }

    fun connect(device: Device) {
        device.connect()
    }

    fun disconnect(device: Device) {
        device.disconnect()
    }

    fun messages(): List<String> {
        return indiEventHandler.messages()
    }

    fun properties(device: Device): Collection<PropertyVector<*, *>> {
        return device.properties.values
    }

    fun sendProperty(device: Device, vector: INDISendProperty) {
        when (vector.type) {
            PropertyType.NUMBER -> {
                val elements = vector.items.map { it.name to "${it.value}".toDouble() }
                device.sendNewNumber(vector.name, elements)
            }
            PropertyType.SWITCH -> {
                val elements = vector.items.map { it.name to "${it.value}".toBooleanStrict() }
                device.sendNewSwitch(vector.name, elements)
            }
            PropertyType.TEXT -> {
                val elements = vector.items.map { it.name to "${it.value}" }
                device.sendNewText(vector.name, elements)
            }
            else -> Unit
        }
    }

    @Synchronized
    fun indiServerStart(request: INDIServerStart) {
        if (!serverHandler.isRunning) {
            val commands = mutableListOf("indiserver", "-p", "${request.port}", "-r", "${request.restarts}", "-d", "0")
            if (request.verbose) commands.add("-v")
            request.executables.map { it.split(',').filter { it.isNotBlank() } }.flatten().toSet().forEach(commands::add)

            val commandLine = CommandLine(commands)
            commandLine.executeAsync(executorService, serverHandler)
        }
    }

    @Synchronized
    fun indiServerStop() {
        if (serverHandler.isRunning) {
            serverHandler.kill()
        }
    }

    fun indiServerLogs(page: Int, pageSize: Int): List<String> {
        val start = max(0, page - 1) * pageSize

        return synchronized(serverLogs) {
            if (start in serverLogs.indices) {
                val end = min(start + pageSize, serverLogs.size)
                serverLogs.subList(start, end).toList()
            } else {
                emptyList()
            }
        }
    }

    fun indiServerStatus(): INDIServerStatus {
        return INDIServerStatus(serverHandler.isRunning, serverHandler.pid)
    }

    override fun onLineRead(line: String) {
        synchronized(serverLogs) {
            serverLogs.add(line)

            if (serverLogs.size > MAX_SERVER_LOGS) {
                serverLogs.removeFirst()
            }
        }
    }

    override fun onExited(exitCode: Int, exception: Throwable?) {
        if (exception != null) LOG.error("indi server exited. code={}", exitCode, exception)
        else LOG.warn("indi server exited. code={}", exitCode)
    }

    companion object {

        private const val MAX_SERVER_LOGS = 1024

        private val LOG = loggerFor<INDIService>()
    }
}
