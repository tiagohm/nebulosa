package nebulosa.api.data.responses

import nebulosa.nova.astrometry.Constellation

data class ComputedCoordinateResponse(
    val rightAscension: String,
    val declination: String,
    val azimuth: String,
    val altitude: String,
    val constellation: Constellation,
)
