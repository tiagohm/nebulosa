package nebulosa.api.mounts

import nebulosa.nova.astrometry.Constellation

data class ComputedLocation(
    var rightAscension: String = "",
    var declination: String = "",
    var rightAscensionJ2000: String = "",
    var declinationJ2000: String = "",
    var azimuth: String = "",
    var altitude: String = "",
    var constellation: Constellation? = null,
    var lst: String = "",
    var meridianAt: String = "",
    var timeLeftToMeridianFlip: String = "",
)
