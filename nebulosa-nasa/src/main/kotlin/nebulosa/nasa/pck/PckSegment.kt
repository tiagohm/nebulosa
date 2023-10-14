package nebulosa.nasa.pck

import nebulosa.erfa.PositionAndVelocity
import nebulosa.time.InstantOfTime

interface PckSegment {

    /**
     * Official ephemeris name, like 'DE-0430LE-0430'.
     */
    val source: String

    /**
     * Initial epoch, as seconds from J2000.
     */
    val start: Double

    /**
     * Final epoch, as seconds from J2000.
     */
    val end: Double

    /**
     * Integer body identifier.
     */
    val body: Int

    /**
     * Integer frame identifier.
     */
    val frame: Int

    /**
     * Data type identifier.
     */
    val type: Int

    /**
     * Index where segment starts.
     */
    val startIndex: Int

    /**
     * Index where segment ends.
     */
    val endIndex: Int

    /**
     * Computes the component and differentials values for the TDB [time].
     *
     * If [derivative] is true, returns a tuple containing both the angle and its derivative;
     * otherwise simply returns the angles.
     */
    fun compute(
        time: InstantOfTime,
        derivative: Boolean = false,
    ): PositionAndVelocity
}
