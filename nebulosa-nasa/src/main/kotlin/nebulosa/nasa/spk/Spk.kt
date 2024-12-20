package nebulosa.nasa.spk

import nebulosa.nasa.daf.Daf
import nebulosa.nasa.daf.Summary
import java.io.IOException

/**
 * A JPL SPK (Spacecraft and Planet Kernel) ephemeris kernel for computing positions and velocities.
 *
 * @see <a href="https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/C/req/spk.html">SPK Reference</a>
 * @see <a href="https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/">SPK Files</a>
 */
data class Spk(@JvmField internal val daf: Daf) : AutoCloseable, Collection<SpkSegment> {

    init {
        daf.read()
    }

    private val segments by lazy {
        daf.summaries
            .map { it.makeSegment(this) }
            .associateBy { it.center to it.target }
    }

    override fun close() = daf.close()

    override val size
        get() = segments.size

    override fun isEmpty() = segments.isEmpty()

    override fun contains(element: SpkSegment) = segments.containsValue(element)

    override fun containsAll(elements: Collection<SpkSegment>) = elements.all { it in this }

    override fun iterator() = segments.values.iterator()

    operator fun get(center: Int, target: Int) = segments[center to target]

    companion object {

        private fun Summary.makeSegment(spk: Spk): SpkSegment {
            val start = doubleAt(0)
            val end = doubleAt(1)
            val target = intAt(0)
            val center = intAt(1)
            val frame = intAt(2)
            val type = intAt(3)
            val startIndex = intAt(4)
            val endIndex = intAt(5)

            return when (type) {
                9 -> Type9Segment(spk, name, start, end, center, target, frame, type, startIndex, endIndex)
                2, 3 -> Type2And3Segment(spk, name, start, end, center, target, frame, type, startIndex, endIndex)
                21 -> Type21Segment(spk, name, start, end, center, target, frame, type, startIndex, endIndex)
                else -> throw IOException("Only binary SPK data types 2, 3, 9 and 21 are supported")
            }
        }
    }
}
