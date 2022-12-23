package nebulosa.server

import com.google.protobuf.Empty
import com.google.protobuf.StringValue
import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.stub.StreamObserver
import nebulosa.grpc.*
import nebulosa.grpc.NebulosaGrpc.NebulosaImplBase
import nebulosa.indi.devices.ConnectionType
import nebulosa.server.connection.ConnectionService
import nebulosa.server.equipments.EquipmentService
import nebulosa.server.equipments.EquipmentType
import nebulosa.server.equipments.cameras.CameraService
import nebulosa.server.equipments.toCameraEquipment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.Closeable
import java.net.InetSocketAddress

class NebulosaServer(
    val host: String = "0.0.0.0",
    private val port: Int = 7009,
) : NebulosaImplBase(), Closeable, KoinComponent {

    private val connectionService by inject<ConnectionService>()
    private val equipmentService by inject<EquipmentService>()
    private val cameraService by inject<CameraService>()

    @Volatile private var server: Server? = null

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
        ConnectResponse.newBuilder()
            .setConnected(connectionService.isConnected())
            .build()
            .also(responseObserver::onNext)
        responseObserver.onCompleted()
    }

    override fun disconnect(
        request: Empty?,
        responseObserver: StreamObserver<ConnectResponse>,
    ) {
        connectionService.disconnect()
        isConnected(null, responseObserver)
    }

    override fun listCameras(
        request: Empty?,
        responseObserver: StreamObserver<CameraEquipment>,
    ) {
        for (camera in cameraService.list()) {
            if (equipmentService.imagingCamera == null || equipmentService.imagingCamera !== camera) {
                responseObserver.onNext(camera.toCameraEquipment())
            }
        }

        responseObserver.onCompleted()
    }

    override fun cameraByName(
        request: StringValue,
        responseObserver: StreamObserver<CameraEquipment>,
    ) {
        val camera = cameraService.list().firstOrNull { it.name == request.value }
        if (camera == null) responseObserver.onError(IllegalArgumentException("camera not found"))
        else responseObserver.onNext(camera.toCameraEquipment())
        responseObserver.onCompleted()
    }

    override fun open(
        request: OpenRequest,
        responseObserver: StreamObserver<OpenResponse>,
    ) {
        try {
            equipmentService.open(
                request.name,
                EquipmentType.valueOf(request.deviceType),
                ConnectionType.valueOf(request.connectionType),
                null,
            )

            OpenResponse.newBuilder()
                .setConnected(true)
                .build()
                .also(responseObserver::onNext)
        } catch (e: Throwable) {
            e.printStackTrace()
            OpenResponse.newBuilder()
                .setConnected(false)
                .build()
                .also(responseObserver::onNext)
        } finally {
            responseObserver.onCompleted()
        }
    }

    @Synchronized
    override fun close() {
        server?.shutdownNow()
        server?.awaitTermination()
        server = null
    }
}
