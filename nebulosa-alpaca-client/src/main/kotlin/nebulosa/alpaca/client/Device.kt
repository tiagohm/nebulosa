package nebulosa.alpaca.client

import nebulosa.alpaca.api.AlpacaService
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.indi.protocol.DefText
import nebulosa.indi.protocol.DefTextVector

internal class Device(
    val device: ConfiguredDevice,
    val service: AlpacaService,
    val client: AlpacaClient,
) {

    @Volatile private var isConnected = false

    fun process(count: Int) {
        if (device.type == "CAMERA") processCamera(count)
    }

    private fun processCamera(count: Int) {
        service.camera.isConnected(device.number).enqueue(DeviceCallback {
            if (count % 30 == 0) {
                if (count == 0) {
                    client.sendMessageToServer(
                        DefTextVector.Builder()
                            .device(device.name).name("DEVICE_INFO").group("").label("")
                            .add(DefText.Builder().build())
                            .add(DefText.Builder().build())
                            .build()
                    )
                }
            }
        })
    }

    private inner class IsConnected : DeviceCallback<Boolean> {

        override fun onSuccess(data: Boolean?) {

        }
    }
}
