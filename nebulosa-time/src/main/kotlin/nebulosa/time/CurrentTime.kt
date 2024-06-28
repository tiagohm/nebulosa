package nebulosa.time

import nebulosa.common.time.Stopwatch

data object CurrentTime : InstantOfTime() {

    @JvmField @Volatile var ELAPSED_INTERVAL = 5L

    private val stopwatch = Stopwatch()

    init {
        stopwatch.start()
    }

    private var time = UTC.now()
        get() {
            synchronized(stopwatch) {
                if (stopwatch.elapsedSeconds >= ELAPSED_INTERVAL) {
                    stopwatch.reset()
                    field = UTC.now()
                }
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
