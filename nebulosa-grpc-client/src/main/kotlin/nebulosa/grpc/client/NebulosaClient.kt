package nebulosa.grpc.client

import com.google.protobuf.Empty
import io.grpc.InsecureChannelCredentials
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import nebulosa.grpc.MountGetDeclinationRequest
import nebulosa.grpc.MountGetRightAscensionRequest
import nebulosa.grpc.MountIsConnectedRequest
import nebulosa.grpc.NebulosaGrpc
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

data class NebulosaClient(private val address: SocketAddress) : AutoCloseable {

    constructor(host: String = "localhost", port: Int = 7654)
            : this(InetSocketAddress(host, port))

    private val channel = NettyChannelBuilder
        .forAddress(address, InsecureChannelCredentials.create())
        .keepAliveTime(1, TimeUnit.MINUTES)
        .keepAliveTimeout(5, TimeUnit.SECONDS)
        .keepAliveWithoutCalls(true)
        .build()

    private val stub = NebulosaGrpc.newBlockingStub(channel)
    private val eventListeners = LinkedHashSet<EventListener>()

    private val eventsThread = thread(isDaemon = true, name = "Nebulosa Client Events Thread") {
        while (true) {
            for (event in stub.events(EMPTY_REQUEST)) {
                eventListeners.forEach { it.onEventReceived(event.type, event.device) }
            }
        }
    }

    fun registerEventListener(listener: EventListener) {
        eventListeners.add(listener)
    }

    fun unregisterEventListener(listener: EventListener) {
        eventListeners.remove(listener)
    }

    fun mounts(): List<String> {
        return stub.mountList(EMPTY_REQUEST).mountsList.map { it.device }
    }

    fun mountIsConnected(device: String): Boolean {
        val request = MountIsConnectedRequest.newBuilder().setDevice(device).build()
        return stub.mountIsConnected(request).connected
    }

    fun mountRightAscension(device: String): Double {
        val request = MountGetRightAscensionRequest.newBuilder().setDevice(device).build()
        return stub.mountGetRightAscension(request).rightAscension
    }

    fun mountDeclination(device: String): Double {
        val request = MountGetDeclinationRequest.newBuilder().setDevice(device).build()
        return stub.mountGetDeclination(request).declination
    }

    override fun close() {
        eventsThread.interrupt()
        channel.shutdownNow()
    }

    companion object {

        @JvmStatic private val EMPTY_REQUEST = Empty.getDefaultInstance()
    }
}
