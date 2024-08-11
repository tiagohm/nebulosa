package nebulosa.grpc.server

import io.grpc.InsecureServerCredentials
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.Executor

data class NebulosaServer(
    private val address: SocketAddress,
    private val executor: Executor? = null,
) : Runnable, AutoCloseable {

    private val service = NebulosaService()

    constructor(host: String = "localhost", port: Int = 7654, executor: Executor? = null)
            : this(InetSocketAddress(host, port), executor)

    private val server = NettyServerBuilder.forAddress(address, InsecureServerCredentials.create())
        .executor(executor)
        .addService(service)
        .build()

    override fun run() {
        server.start()
    }

    override fun close() {
        service.close()
        server.shutdownNow()
    }
}
