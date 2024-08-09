package nebulosa.api.image

data class AnnotateImageRequest(
    @JvmField val starsAndDSOs: Boolean = true,
    @JvmField val minorPlanets: Boolean = false,
    @JvmField val minorPlanetMagLimit: Double = 12.0,
    @JvmField val includeMinorPlanetsWithoutMagnitude: Boolean = false,
    @JvmField val useSimbad: Boolean = false,
)
