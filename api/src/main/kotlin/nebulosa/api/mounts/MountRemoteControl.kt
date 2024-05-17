package nebulosa.api.mounts

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.INDIDeviceProvider
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEquatorialCoordinatesChanged
import nebulosa.lx200.protocol.LX200MountHandler
import nebulosa.lx200.protocol.LX200MountHandlerAdapter
import nebulosa.lx200.protocol.LX200ProtocolServer
import nebulosa.math.Angle
import nebulosa.netty.NettyServer
import nebulosa.stellarium.protocol.StellariumMountHandler
import nebulosa.stellarium.protocol.StellariumMountHandlerAdapter
import nebulosa.stellarium.protocol.StellariumProtocolServer
import java.io.Closeable
import java.time.OffsetDateTime

data class MountRemoteControl(
    @JvmField val type: MountRemoteControlType,
    @field:JsonIgnore @JvmField val server: NettyServer,
    @JvmField val mount: Mount,
) : StellariumMountHandler, LX200MountHandler, DeviceEventHandler, Closeable {

    @JsonIgnore private val stellariumAdapter = StellariumMountHandlerAdapter(mount)
    @JsonIgnore private val lx200Adapter = LX200MountHandlerAdapter(mount)
    @JsonIgnore private val deviceProvider = mount.sender as? INDIDeviceProvider

    init {
        if (server is StellariumProtocolServer) {
            deviceProvider?.registerDeviceEventHandler(this)
            server.attachMountHandler(this)
        } else if (server is LX200ProtocolServer) {
            server.attachMountHandler(this)
        }
    }

    val host
        get() = server.host

    val port
        get() = server.port

    val running
        get() = server.running

    override val rightAscension
        get() = if (server is StellariumProtocolServer) stellariumAdapter.rightAscension
        else lx200Adapter.rightAscension

    override val declination
        get() = if (server is StellariumProtocolServer) stellariumAdapter.declination
        else lx200Adapter.declination

    override val latitude
        get() = lx200Adapter.latitude

    override val longitude
        get() = lx200Adapter.longitude

    override val slewing
        get() = lx200Adapter.slewing

    override val tracking
        get() = lx200Adapter.tracking

    override val parked
        get() = lx200Adapter.parked

    override fun goTo(rightAscension: Angle, declination: Angle) {
        if (server is StellariumProtocolServer) stellariumAdapter.goTo(rightAscension, declination)
        else lx200Adapter.goTo(rightAscension, declination)
    }

    override fun syncTo(rightAscension: Angle, declination: Angle) {
        lx200Adapter.syncTo(rightAscension, declination)
    }

    override fun abort() {
        lx200Adapter.abort()
    }

    override fun moveNorth(enabled: Boolean) {
        lx200Adapter.moveNorth(enabled)
    }

    override fun moveSouth(enabled: Boolean) {
        lx200Adapter.moveSouth(enabled)
    }

    override fun moveWest(enabled: Boolean) {
        lx200Adapter.moveWest(enabled)
    }

    override fun moveEast(enabled: Boolean) {
        lx200Adapter.moveEast(enabled)
    }

    override fun time(time: OffsetDateTime) {
        lx200Adapter.time(time)
    }

    override fun coordinates(longitude: Angle, latitude: Angle) {
        lx200Adapter.coordinates(longitude, latitude)
    }

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event is MountEquatorialCoordinatesChanged) {
            (server as StellariumProtocolServer).sendCurrentPosition(mount.rightAscension, mount.declination)
        }
    }

    override fun onConnectionClosed() {
        close()
    }

    override fun close() {
        lx200Adapter.abort()
        deviceProvider?.unregisterDeviceEventHandler(this)
        server.close()
    }
}
