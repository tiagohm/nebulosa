package nebulosa.api.data.responses

import nebulosa.indi.device.mount.*
import nebulosa.math.AngleFormatter

data class MountResponse(
    val name: String,
    val connected: Boolean,
    val slewing: Boolean,
    val tracking: Boolean,
    val canAbort: Boolean,
    val canSync: Boolean,
    val canGoTo: Boolean,
    val canHome: Boolean,
    val slewRates: List<SlewRate>,
    val slewRate: SlewRate?,
    val mountType: MountType,
    val trackModes: List<TrackMode>,
    val trackMode: TrackMode,
    val pierSide: PierSide,
    val guideRateWE: Double,
    val guideRateNS: Double,
    val rightAscension: String,
    val declination: String,
) {

    constructor(mount: Mount) : this(
        mount.name,
        mount.connected,
        mount.slewing,
        mount.tracking,
        mount.canAbort,
        mount.canSync,
        mount.canGoTo,
        mount.canHome,
        mount.slewRates,
        mount.slewRate,
        mount.mountType,
        mount.trackModes,
        mount.trackMode,
        mount.pierSide,
        mount.guideRateWE,
        mount.guideRateNS,
        mount.rightAscension.format(AngleFormatter.HMS),
        mount.declination.format(AngleFormatter.SIGNED_DMS),
    )
}
