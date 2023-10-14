package nebulosa.api.guiding

import nebulosa.common.concurrency.Incrementer
import nebulosa.guiding.GuideStep
import java.util.*
import kotlin.math.max
import kotlin.math.min

class GuideStepHistory private constructor(private val data: LinkedList<HistoryStep>) : List<HistoryStep> by data {

    constructor() : this(LinkedList())

    private val id = Incrementer()
    private val rms = RMS()

    var maxHistorySize = 100
        set(value) {
            field = max(100, min(value, 1000))
        }

    private fun add(step: HistoryStep) {
        while (data.size >= maxHistorySize) {
            data.pop()
        }

        data.add(step)
    }

    @Synchronized
    fun addGuideStep(guideStep: GuideStep): HistoryStep {
        if (rms.size == maxHistorySize) {
            while (isNotEmpty()) {
                val removedStep = data.pop()
                removedStep.guideStep ?: continue
                rms.removeDataPoint(removedStep.guideStep.raDistance, removedStep.guideStep.decDistance)
                break
            }
        }

        rms.addDataPoint(guideStep.raDistance, guideStep.decDistance)

        return HistoryStep(id.increment(), rms.rightAscension, rms.declination, rms.total, guideStep)
            .also(::add)
    }

    @Synchronized
    fun addDither(dx: Double, dy: Double) {
        add(HistoryStep(id = id.increment(), ditherX = dx, ditherY = dy))
    }

    @Synchronized
    fun clear() {
        data.clear()
        rms.clear()
    }
}
