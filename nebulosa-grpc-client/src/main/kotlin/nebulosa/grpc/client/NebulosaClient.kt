package nebulosa.grpc.client

import io.grpc.InsecureChannelCredentials
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import nebulosa.grpc.BarkRequest
import nebulosa.grpc.DogGrpc
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.TimeUnit

data class NebulosaClient(private val address: SocketAddress) {

    constructor(host: String = "localhost", port: Int = 7654)
            : this(InetSocketAddress(host, port))

    private val channel = NettyChannelBuilder
        .forAddress(address, InsecureChannelCredentials.create())
        .keepAliveTime(1, TimeUnit.MINUTES)
        .keepAliveTimeout(5, TimeUnit.SECONDS)
        .keepAliveWithoutCalls(true)
        .build()

    private val stub = DogGrpc.newFutureStub(channel)

    fun bark() {
        stub.bark(BarkRequest.newBuilder().build()).get().message.also(::println)
    }
}
