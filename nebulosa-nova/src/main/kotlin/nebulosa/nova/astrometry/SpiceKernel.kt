package nebulosa.nova.astrometry

import nebulosa.nasa.spk.Spk
import java.io.IOException

class SpiceKernel(vararg spks: Spk) {

    private val segmentsByTarget: Map<Number, Body>
    private val cache = HashMap<Int, Body>()

    init {
        segmentsByTarget = HashMap(64)

        for (spk in spks) {
            spk.map {
                val p = when (it.type) {
                    2 -> ChebyshevPosition(it)
                    3, 21 -> ChebyshevPositionAndVelocity(it)
                    else -> throw IOException("SPK data type ${it.type} is not yet supported")
                }

                if (it.target !in segmentsByTarget) {
                    segmentsByTarget[it.target] = p
                }
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
