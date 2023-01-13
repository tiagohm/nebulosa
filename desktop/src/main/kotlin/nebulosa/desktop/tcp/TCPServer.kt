package nebulosa.desktop.tcp

import okio.Buffer
import okio.buffer
import okio.sink
import okio.source
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

abstract class TCPServer(
    val host: String = "0.0.0.0",
    val port: Int,
) : ArrayList<TCPServer.Client>(), Closeable {

    abstract class Client(val socket: Socket) : Runnable, Closeable {

        @Volatile private var thread: Thread? = null

        protected val input = socket.getInputStream().source().buffer()
        protected val output = socket.getOutputStream().sink().buffer()
        protected val buffer = Buffer()

        open fun start() {
            thread = Thread(this)
            thread!!.start()
        }

        abstract fun processMessage(): Boolean

        @Suppress("ControlFlowWithEmptyBody")
        final override fun run() {
            LOG.info("running TCP client thread")

            try {
                while (processMessage());
            } catch (_: InterruptedException) {
            } catch (e: Throwable) {
                LOG.error("socket read error", e)
            } finally {
                close()
            }

            LOG.info("TCP client thread finished")
        }

        override fun close() {
            thread?.interrupt()
            thread = null

            runCatching { socket.close() }
        }
    }

    private val serverSocket = ServerSocket()
    private val acceptorThread = Thread(::acceptSocket)

    @Volatile private var closed = false

    val isClosed get() = serverSocket.isClosed || closed

    fun start() {
        check(!isClosed) { "closed" }
        serverSocket.bind(InetSocketAddress(host, port))
        acceptorThread.start()
    }

    protected abstract fun acceptSocket(socket: Socket): Client?

    private fun acceptSocket() {
        try {
            while (true) {
                val socket = serverSocket.accept() ?: continue

                socket.keepAlive = true

                val client = acceptSocket(socket)

                if (client != null) {
                    add(client)

                    client.start()

                    LOG.info("new client: $socket")
                } else {
                    socket.close()
                }
            }
        } catch (_: InterruptedException) {
        } catch (e: Throwable) {
            LOG.error("socket accept error", e)
        }
    }

    override fun close() {
        if (closed) return

        acceptorThread.interrupt()

        forEach(Client::close)
        clear()

        try {
            serverSocket.close()
        } finally {
            closed = true
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(TCPServer::class.java)
    }
}
