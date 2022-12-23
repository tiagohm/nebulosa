package nebulosa.server

import com.google.protobuf.Empty
import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.stub.StreamObserver
import nebulosa.grpc.ConnectRequest
import nebulosa.grpc.ConnectResponse
import nebulosa.grpc.NebulosaGrpc.NebulosaImplBase
import nebulosa.grpc.connectResponse
import nebulosa.server.connection.ConnectionService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.Closeable
import java.net.InetSocketAddress

class NebulosaServer(
    val host: String = "0.0.0.0",
    private val port: Int = 7009,
) : NebulosaImplBase(), Closeable, KoinComponent {

    private val connectionService by inject<ConnectionService>()
    private var server: Server? = null

    @Synchronized
    fun start() {
        if (server == null) {
            server = NettyServerBuilder.forAddress(InetSocketAddress(host, port))
                .addService(this)
                .build()
            server!!.start()
        }
    }

    override fun connect(
        request: ConnectRequest,
        responseObserver: StreamObserver<ConnectResponse>,
    ) {
        try {
            connectionService.connect(request.host, request.port)
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            isConnected(null, responseObserver)
        }
    }

    override fun isConnected(
        request: Empty?,
        responseObserver: StreamObserver<ConnectResponse>,
    ) {
        responseObserver.onNext(connectResponse { connected = connectionService.isConnected() })
        responseObserver.onCompleted()
    }

    override fun disconnect(
        request: Empty?,
        responseObserver: StreamObserver<ConnectResponse>,
    ) {
        connectionService.disconnect()
        isConnected(null, responseObserver)
    }

    @Synchronized
    override fun close() {
        server?.shutdownNow()
        server?.awaitTermination()
        server = null
    }
}
