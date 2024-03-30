package nebulosa.indi.device.camera

import nebulosa.indi.device.CompanionDevice

interface GuideHead : Camera, CompanionDevice {

    override val main: Camera
}
