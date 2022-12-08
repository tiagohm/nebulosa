package nebulosa.nova.astrometry

import nebulosa.nasa.spk.Spk
import nebulosa.nasa.spk.SpkSegment
import java.io.IOException

class SpiceKernel(
    vararg spks: Spk,
    replace: (SpkSegment) -> Boolean = { _ -> false },
) {

    // private val segments: List<Body>
    // private val segmentCodes: Set<Int>
    private val segmentsByTarget: Map<Number, Body>
    private val cache = HashMap<Int, Body>()

    init {
        segmentsByTarget = HashMap(64)
        // segments = ArrayList(spk.size)
        // segmentCodes = HashSet(segments.size * 2)

        for (spk in spks) {
            spk.map {
                val p = when (it.type) {
                    2 -> ChebyshevPosition(it)
                    3 -> ChebyshevPositionAndVelocity(it)
                    else -> throw IOException("SPK data type ${it.type} is not yet supported")
                }

                if (it.target !in segmentsByTarget || replace(it)) {
                    segmentsByTarget[it.target] = p
                }

                // segments.add(p)
                // segmentCodes.add(it.center)
                // segmentCodes.add(it.target)
            }
        }
    }

    operator fun get(code: Int): Body {
        if (code in cache) return cache[code]!!
        val chain = center(code)
        if (chain.isEmpty()) throw IllegalStateException("missing kernel: $code")
        val body = Body(chain.first().center, chain.last().target, chain)
        cache[code] = body
        return body
    }

    private fun center(target: Number): List<Body> {
        val segments = ArrayList<Body>(4)
        var code = target

        while (code in segmentsByTarget) {
            val segment = segmentsByTarget[code]!!
            segments.add(segment)
            code = segment.center
        }

        return segments.reversed()
    }
}
