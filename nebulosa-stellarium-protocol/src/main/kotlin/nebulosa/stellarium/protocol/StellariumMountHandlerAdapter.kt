package nebulosa.stellarium.protocol

import nebulosa.indi.device.mount.Mount
import nebulosa.math.Angle

data class StellariumMountHandlerAdapter(private val mount: Mount) : StellariumMountHandler {

    override val rightAscension
        get() = mount.rightAscension

    override val declination
        get() = mount.declination

    override fun goTo(rightAscension: Angle, declination: Angle) {
        mount.goTo(rightAscension, declination)
    }
}
