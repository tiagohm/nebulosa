package nebulosa.api.data.responses

import nebulosa.indi.device.camera.Camera

data class CameraResponse(
    val name: String,
    val connected: Boolean,
) {

    constructor(camera: Camera) : this(
        camera.name,
        camera.connected,
    )
}
