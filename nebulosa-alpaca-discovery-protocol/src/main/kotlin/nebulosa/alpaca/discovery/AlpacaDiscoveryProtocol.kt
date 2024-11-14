package nebulosa.alpaca.discovery

import nebulosa.log.loggerFor
import java.io.IOException
import java.net.*

/**
 * Discoveries Windows COM based drivers through ASCOM's registry-based Chooser capability.
 *
 * @see <a href="https://ascom-standards.org/Developer/ASCOM%20Alpaca%20API%20Reference.pdf">ASCOM Alpaca Reference</a>
 * @see <a href="https://ascom-standards.org/api/?urls.primaryName=ASCOM%20Alpaca%20Management%20API">ASCOM Alpaca Management API</a>
 * @see <a href="https://ascom-standards.org/api/?urls.primaryName=ASCOM%20Alpaca%20Device%20API">ASCOM Alpaca Device API</a>
 */
class AlpacaDiscoveryProtocol : Runnable, AutoCloseable {

    @Volatile private var running = false

    private val socket = DatagramSocket(0)
    private val listeners = arrayListOf<DiscoveryListener>()

    init {
        socket.broadcast = true
    }

    fun registerDiscoveryListener(listener: DiscoveryListener) {
        listeners.add(listener)
    }

    fun unregisterDiscoveryListener(listener: DiscoveryListener) {
        listeners.remove(listener)
    }

    @Synchronized
    override fun run() {
        running = true

        val discoveryMessage = ALPACA_DISCOVERY_MESSAGE.encodeToByteArray()

        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()

            while (interfaces.hasMoreElements()) {
                val i = interfaces.nextElement()

                for (address in i.interfaceAddresses) {
                    if (address.address is Inet4Address) {
                        try {
                            val message = DatagramPacket(discoveryMessage, discoveryMessage.size, address.broadcast, 32227)
                            socket.send(message)
                        } catch (_: InterruptedException) {
                            Thread.currentThread().interrupt()
                            break
                        } catch (e: IOException) {
                            LOG.error("socket IPv4 send error", e)
                        }
                    }
                }
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: SocketException) {
            LOG.error("socket error", e)
        }

        try {
            val message = DatagramPacket(discoveryMessage, discoveryMessage.size, InetAddress.getByName("ff12::00a1:9aca"), 32227)
            socket.send(message)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: IOException) {
            LOG.error("socket IPv6 send error", e)
        }

        val responseBuffer = ByteArray(255)

        while (running) {
            val packet = DatagramPacket(responseBuffer, responseBuffer.size)

            try {
                socket.receive(packet)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            } catch (e: IOException) {
                LOG.error("socket receive error", e)
                break
            }

            val message = packet.data.decodeToString(0, packet.length)
            val port = ALPACA_PORT_REGEX.find(message)?.groupValues?.get(1)?.toIntOrNull() ?: continue
            LOG.info("server found at {}:{}", packet.address, port)
            listeners.forEach { it.onServerFound(packet.address, port) }
        }
    }

    override fun close() {
        running = false

        socket.close()
    }

    companion object {

        private const val ALPACA_DISCOVERY_MESSAGE = "alpacadiscovery1"

        private val ALPACA_PORT_REGEX = Regex("\\{\"AlpacaPort\":(\\d+)}")
        private val LOG = loggerFor<AlpacaDiscoveryProtocol>()
    }
}
