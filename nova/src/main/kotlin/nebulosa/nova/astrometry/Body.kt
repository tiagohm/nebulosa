package nebulosa.nova.astrometry

import nebulosa.constants.DAYSEC
import nebulosa.constants.SPEED_OF_LIGHT
import nebulosa.math.Vector3D
import nebulosa.time.InstantOfTime
import nebulosa.time.TDB
import kotlin.math.abs

interface Body : PositionAndVelocityOverTime, Observable, Iterable<Body> {

    val center: Number

    val target: Number

    override fun compute(time: InstantOfTime): Pair<Vector3D, Vector3D> {
        var position = Vector3D()
        var velocity = Vector3D()

        for (body in this) {
            val pv = body.compute(time)

            position += pv.first
            velocity += pv.second
        }

        return position to velocity
    }

    override fun observe(observer: ICRF): Pair<Vector3D, Vector3D> {
        require(center.toInt() == 0) {
            "you can only observe a body whose vector's center is the Solar System Barycenter," +
                " but this vector has the center $center"
        }

        return if (observer.target.toInt() == Int.MIN_VALUE) {
            val (p, v) = compute(observer.time)
            (p - observer.position) to (v - observer.velocity)
        } else {
            correctForLightTravelTime(observer, this)
        }
    }

    /**
     * At the [time], computes the [target]'s position relative to the [center].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : ICRF> at(time: InstantOfTime): T {
        val (position, velocity) = compute(time)
        val i = ICRF.of(position, velocity, time, center, target)
        return i as T
    }

    /**
     * Produces a new [Body] that, when invoked with [at], returns the sum of the vectors.
     */
    operator fun plus(body: Body): Body {
        var a = this
        var b = body

        if (target != body.center) {
            require(body.target == center) {
                "you can only add two vectors " +
                    "if the target where one of the vectors ends " +
                    "is the center where the other vector starts"
            }

            a = body
            b = this
        }

        return Sum(a.center, b.target, a + b)
    }

    /**
     * Produces a new [Body] that, when invoked with [at],
     * returns the difference of the vectors.
     */
    operator fun minus(body: Body): Body {
        require(center == body.center) { "you can only subtract two vectors if they both start at the same center" }
        val v = body.map { -it }.reversed()
        return Sum(body.target, target, v + this)
    }

    /**
     * Produces a new [Body] that, when invoked with [at],
     * returns the reverse of the vectors.
     */
    operator fun unaryMinus(): Body = Reversed(this)

    override fun iterator() = arrayOf(this).iterator()

    private data class Sum(
        override val center: Number,
        override val target: Number,
        private val bodies: Iterable<Body>,
    ) : Body {

        override fun iterator() = bodies.iterator()
    }

    private data class Reversed(
        val body: Body,
        override val center: Number = body.target,
        override val target: Number = body.center,
    ) : Body {

        override fun compute(time: InstantOfTime) = body.compute(time).let { -it.first to -it.second }

        override fun unaryMinus() = body

        override fun iterator() = body.iterator()
    }

    companion object {

        @JvmStatic
        @JvmName("of")
        operator fun invoke(
            center: Number,
            target: Number,
            bodies: Iterable<Body>,
        ): Body = Sum(center, target, bodies)

        /**
         * Computes the light travel time correction
         * from [observer]'s position to [body]'s position.
         */
        internal fun correctForLightTravelTime(observer: ICRF, body: Body): Pair<Vector3D, Vector3D> {
            var time = observer.time.tdb
            val whole = time.whole
            val fraction = time.fraction
            var pv = body.compute(time)
            var value = 0.0

            for (i in 0..9) {
                val position = pv.first - observer.position
                val distance = position.length
                val lightTime = distance * (1000.0 / (SPEED_OF_LIGHT * DAYSEC))

                if (abs(lightTime - value) <= 1E-12) break

                time = TDB(whole, fraction - lightTime)
                pv = body.compute(time)
                value = lightTime
            }

            return (pv.first - observer.position) to (pv.second - observer.velocity)
        }
    }
}
