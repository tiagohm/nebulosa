package nebulosa.nasa.spk

import nebulosa.math.Vector3D
import nebulosa.time.InstantOfTime

/**
 * A single segment of an SPK file.
 */
interface SpkSegment {

    /**
     * SPK file source.
     */
    val spk: Spk

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
     * Center identifier.
     */
    val center: Int

    /**
     * Target identifier.
     */
    val target: Int

    /**
     * Frame identifier.
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
     */
    fun compute(time: InstantOfTime): Pair<Vector3D, Vector3D>
}
