package nebulosa.time

/**
 * Base class for support floating point Besselian and Julian epoch dates.
 */
sealed interface TimeEpochDate {

    /**
     * The epoch date.
     */
    val epoch: Double
}
