package nebulosa.desktop.logic.io

import okio.Buffer
import okio.buffer
import okio.sink
import okio.source
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.InterruptedIOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

abstract class TCPServer : ArrayList<TCPServer.Client>(), Closeable {

    var host = ""
        private set

    var port = -1
        private set

    @Volatile private var serverSocket: ServerSocket? = null
    @Volatile private var acceptorThread: Thread? = null

    val running
        get() = serverSocket != null

    protected abstract fun acceptSocket(socket: Socket): Client?

    fun start(host: String, port: Int) {
        close()

        val serverSocket = ServerSocket()
        serverSocket.bind(InetSocketAddress(host, port))

        this.serverSocket = serverSocket

        val acceptorThread = Thread(::acceptSocket)
        acceptorThread.isDaemon = true
        acceptorThread.start()

        this.host = host
        this.port = serverSocket.localPort
        this.acceptorThread = acceptorThread
    }

    private fun acceptSocket() {
        try {
            while (true) {
                val socket = serverSocket!!.accept() ?: continue

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
        } catch (_: InterruptedIOException) {
        } catch (e: Throwable) {
            LOG.error("socket accept error", e)
        }
    }

    override fun close() {
        acceptorThread?.interrupt()
        acceptorThread = null

        serverSocket?.close()
        serverSocket = null

        forEach(Client::close)
        clear()

        host = ""
        port = -1
    }

    abstract class Client(val socket: Socket) : Runnable, Closeable {

        @Volatile private var thread: Thread? = null

        protected val input = socket.getInputStream().source().buffer()
        protected val output = socket.getOutputStream().sink().buffer()
        protected val buffer = Buffer()

        open fun start() {
            thread = Thread(this)
            thread!!.isDaemon = true
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

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(TCPServer::class.java)
    }
}
