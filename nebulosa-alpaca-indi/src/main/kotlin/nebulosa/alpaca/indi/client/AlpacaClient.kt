package nebulosa.alpaca.indi.client

import nebulosa.alpaca.api.AlpacaService
import nebulosa.alpaca.api.DeviceType
import nebulosa.alpaca.indi.device.cameras.ASCOMCamera
import nebulosa.alpaca.indi.device.focusers.ASCOMFocuser
import nebulosa.alpaca.indi.device.mounts.ASCOMMount
import nebulosa.alpaca.indi.device.rotators.ASCOMRotator
import nebulosa.alpaca.indi.device.wheels.ASCOMFilterWheel
import nebulosa.indi.device.AbstractINDIDeviceProvider
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.log.dw
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import java.util.*

data class AlpacaClient(
    val host: String, val port: Int,
    private val httpClient: OkHttpClient? = null,
) : AbstractINDIDeviceProvider() {

    private val service = AlpacaService("http://$host:$port/", httpClient)

    override val id = UUID.randomUUID().toString()

    override fun sendMessageToServer(message: INDIProtocol) = Unit

    fun discovery() {
        val response = service.management.configuredDevices().execute()

        if (response.isSuccessful) {
            val body = response.body() ?: return

            for (device in body.value) {
                when (device.type) {
                    DeviceType.CAMERA -> {
                        with(ASCOMCamera(device, service.camera, this)) {
                            if (registerCamera(this)) {
                                initialize()
                            }
                        }
                    }
                    DeviceType.TELESCOPE -> {
                        with(ASCOMMount(device, service.telescope, this)) {
                            if (registerMount(this)) {
                                initialize()
                            }
                        }
                    }
                    DeviceType.FILTER_WHEEL -> {
                        with(ASCOMFilterWheel(device, service.filterWheel, this)) {
                            if (registerFilterWheel(this)) {
                                initialize()
                            }
                        }
                    }
                    DeviceType.FOCUSER -> {
                        with(ASCOMFocuser(device, service.focuser, this)) {
                            if (registerFocuser(this)) {
                                initialize()
                            }
                        }
                    }
                    DeviceType.ROTATOR -> {
                        with(ASCOMRotator(device, service.rotator, this)) {
                            if (registerRotator(this)) {
                                initialize()
                            }
                        }
                    }
                    DeviceType.DOME -> Unit
                    DeviceType.SWITCH -> Unit
                    DeviceType.COVER_CALIBRATOR -> Unit
                    DeviceType.OBSERVING_CONDITIONS -> Unit
                    DeviceType.SAFETY_MONITOR -> Unit
                    DeviceType.VIDEO -> Unit
                }
            }
        } else {
            val body = response.errorBody()
            LOG.dw("unsuccessful response. code={}, body={}", response.code(), body?.string())
            body?.close()
        }
    }

    companion object {

        private val LOG = loggerFor<AlpacaClient>()
    }
}
