package nebulosa.indi.client.connection

import nebulosa.indi.client.io.INDIProtocolFactory
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.io.INDIConnection
import nebulosa.log.loggerFor
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

data class INDISocketConnection(private val socket: Socket) : INDIConnection {

    constructor(host: String, port: Int = INDIProtocol.DEFAULT_PORT) : this(Socket()) {
        socket.reuseAddress = false
        socket.connect(InetSocketAddress(host, port), 30000)
    }

    val remoteAddress
        get() = socket.remoteSocketAddress as InetSocketAddress

    val localAddress: InetAddress
        get() = socket.localAddress

    val remoteHost: String
        get() = remoteAddress.hostName

    val remoteIP: String
        get() = remoteAddress.address.hostAddress

    val remotePort
        get() = socket.port

    val localPort
        get() = socket.localPort

    override val input by lazy { INDIProtocolFactory.createInputStream(socket.getInputStream()) }

    override val output by lazy { INDIProtocolFactory.createOutputStream(socket.getOutputStream()) }

    override val isOpen
        get() = !socket.isClosed

    override fun close() {
        try {
            socket.close()
        } catch (e: Throwable) {
            LOG.error("socket close error", e)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<INDISocketConnection>()
    }
}
