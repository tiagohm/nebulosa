package nebulosa.guiding.phd2

import nebulosa.phd2.client.PHD2Client

class PHD2Guider(
    host: String,
    port: Int = 4400,
) {

    private val client = PHD2Client(host, port)

    // override fun connect() {
    //     client.registerListener(this)
    //     client.connect()
    // }

    // override fun onEvent(client: PHD2Client, event: PHD2Event) {
    //     if (client === this.client) {
    //         println(event)
    //     }
    // }

    // override fun close() {
    //     client.unregisterListener(this)
    //     client.close()
    // }
}
