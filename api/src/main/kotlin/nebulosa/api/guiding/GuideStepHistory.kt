package nebulosa.api.guiding

import nebulosa.guiding.GuideStep
import nebulosa.util.concurrency.atomic.Incrementer
import java.util.*

class GuideStepHistory private constructor(private val data: LinkedList<HistoryStep>) : List<HistoryStep> by data {

    constructor() : this(LinkedList())

    private val id = Incrementer()
    private val rms = RMS()

    private fun add(step: HistoryStep) {
        data.add(step)
    }

    @Synchronized
    fun addGuideStep(guideStep: GuideStep): HistoryStep {
        rms.addDataPoint(guideStep.raDistance, guideStep.decDistance)

        return HistoryStep(id.increment(), rms.rightAscension, rms.declination, rms.total, guideStep)
            .also(::add)
    }

    @Synchronized
    fun addDither(dx: Double, dy: Double): HistoryStep {
        return HistoryStep(id = id.increment(), ditherX = dx, ditherY = dy).also(::add)
    }

    @Synchronized
    fun clear() {
        data.clear()
        rms.clear()
    }
}
