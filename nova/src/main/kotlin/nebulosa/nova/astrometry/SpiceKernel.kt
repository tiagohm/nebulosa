package nebulosa.nova.astrometry

import nebulosa.nasa.spk.Spk
import java.io.IOException

class SpiceKernel(spk: Spk) {

    private val segments: List<Body>
    private val segmentsByTarget: Map<Number, Body>
    private val segmentCodes: Set<Int>
    private val cache = HashMap<Int, Body>()

    init {
        segments = ArrayList(spk.size)
        segmentsByTarget = HashMap(spk.size)
        segmentCodes = HashSet(segments.size * 2)

        spk.map {
            val p = when (it.type) {
                2 -> ChebyshevPosition(it)
                3 -> ChebyshevPositionAndVelocity(it)
                else -> throw IOException("SPK data type ${it.type} is not yet supported")
            }

            segments.add(p)
            segmentsByTarget[it.target] = p

            segmentCodes.add(it.center)
            segmentCodes.add(it.target)
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
