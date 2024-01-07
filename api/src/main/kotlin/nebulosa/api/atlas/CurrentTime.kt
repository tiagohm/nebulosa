package nebulosa.api.atlas

import nebulosa.time.InstantOfTime
import nebulosa.time.TimeDelta
import nebulosa.time.UTC

object CurrentTime : InstantOfTime() {

    private const val MAX_INTERVAL = 1000L * 30 // 30s.

    @Volatile private var lastTime = 0L

    private var time = UTC.now()
        @Synchronized get() {
            val curTime = System.currentTimeMillis()

            if (curTime - lastTime >= MAX_INTERVAL) {
                lastTime = curTime
                field = UTC.now()
            }

            return field
        }

    override val whole
        get() = time.whole

    override val fraction
        get() = time.fraction

    override fun plus(days: Double): InstantOfTime {
        time = time.plus(days)
        return this
    }

    override fun plus(delta: TimeDelta): InstantOfTime {
        time = time.plus(delta)
        return this
    }

    override fun minus(days: Double): InstantOfTime {
        time = time.minus(days)
        return this
    }

    override fun minus(delta: TimeDelta): InstantOfTime {
        time = time.minus(delta)
        return this
    }

    override val ut1
        get() = time.ut1

    override val utc
        get() = time.utc

    override val tai
        get() = time.tai

    override val tt
        get() = time.tt

    override val tcg
        get() = time.tcg

    override val tdb
        get() = time.tdb

    override val tcb
        get() = time.tcb

    override val m
        get() = time.m

    override val nutationAngles
        get() = time.nutationAngles

    override val precessionMatrix
        get() = time.precessionMatrix

    override val nutationMatrix
        get() = time.nutationMatrix

    override val polarMotionMatrix
        get() = time.polarMotionMatrix

    override val gast
        get() = time.gast

    override val gmst
        get() = time.gmst

    override val era
        get() = time.era

    override val c
        get() = time.c

    override val trueObliquity
        get() = time.trueObliquity

    override val meanObliquity
        get() = time.meanObliquity
}
