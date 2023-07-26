package nebulosa.imaging.algorithms

import kotlin.math.max
import kotlin.math.min

enum class ProtectionMethod {
    MAXIMUM_MASK {
        override fun compute(p0: Float, p1: Float, g: Float, amount: Float): Float {
            val m = max(p0, p1)
            return g * (1f - amount) * (1f - m) + m * g
        }
    },
    ADDITIVE_MASK {
        override fun compute(p0: Float, p1: Float, g: Float, amount: Float): Float {
            val m = min(1f, p0 + p1)
            return g * (1f - amount) * (1f - m) + m * g
        }
    },
    AVERAGE_NEUTRAL {
        override fun compute(p0: Float, p1: Float, g: Float, amount: Float): Float {
            val m = 0.5f * (p0 + p1)
            return min(g, m)
        }
    },
    MAXIMUM_NEUTRAL {
        override fun compute(p0: Float, p1: Float, g: Float, amount: Float): Float {
            val m = max(p0, p1)
            return min(g, m)
        }
    },
    MINIMUM_NEUTRAL {
        override fun compute(p0: Float, p1: Float, g: Float, amount: Float): Float {
            val m = min(p0, p1)
            return min(g, m)
        }
    };

    abstract fun compute(p0: Float, p1: Float, g: Float, amount: Float): Float
}
