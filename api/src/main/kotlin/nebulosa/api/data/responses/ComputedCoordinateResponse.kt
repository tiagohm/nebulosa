package nebulosa.api.data.responses

import nebulosa.nova.astrometry.Constellation

data class ComputedCoordinateResponse(
    val rightAscension: String,
    val declination: String,
    val rightAscensionJ2000: String,
    val declinationJ2000: String,
    val azimuth: String,
    val altitude: String,
    val constellation: Constellation,
    val lst: String,
    val meridianAt: String,
    val timeLeftToMeridianFlip: String,
)
