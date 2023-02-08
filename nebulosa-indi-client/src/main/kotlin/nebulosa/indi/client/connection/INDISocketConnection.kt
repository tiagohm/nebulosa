package nebulosa.indi.client.connection

import nebulosa.indi.client.io.INDIProtocolFactory
import nebulosa.indi.connection.INDIConnection
import nebulosa.indi.protocol.INDIProtocol
import java.net.InetSocketAddress
import java.net.Socket

class INDISocketConnection(private val socket: Socket) : INDIConnection {

    constructor(host: String, port: Int = INDIProtocol.DEFAULT_PORT) : this(Socket()) {
        socket.connect(InetSocketAddress(host, port), 30000)
    }

    val host: String
        get() = socket.localAddress.hostName

    val port: Int
        get() = socket.localPort

    override val input by lazy { INDIProtocolFactory.createInputStream(socket.getInputStream()) }

    override val output by lazy { INDIProtocolFactory.createOutputStream(socket.getOutputStream()) }

    override val isOpen
        get() = !socket.isClosed

    override fun close() {
        var thrown: Throwable? = null

        try {
            socket.shutdownInput()
        } catch (e: Throwable) {
            thrown = e
        }

        try {
            socket.shutdownOutput()
        } catch (e: Throwable) {
            if (thrown == null) {
                thrown = e
            }
        }

        try {
            socket.close()
        } catch (e: Throwable) {
            if (thrown == null) {
                thrown = e
            }
        }

        if (thrown != null) {
            throw thrown
        }
    }
}
