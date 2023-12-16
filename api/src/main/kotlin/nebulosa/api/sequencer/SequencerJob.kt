package nebulosa.api.sequencer

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.*
import nebulosa.api.guiding.DitherAfterExposureStep
import nebulosa.api.guiding.WaitForSettleStep
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.*
import nebulosa.batch.processing.ExecutionContext.Companion.getInt
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.guiding.Guider

// https://cdn.diffractionlimited.com/help/maximdl/Autosave_Sequence.htm
// https://nighttime-imaging.eu/docs/master/site/tabs/sequence/
// https://nighttime-imaging.eu/docs/master/site/sequencer/advanced/advanced/

data class SequencerJob(
    @JvmField val plan: SequencePlanRequest,
    @JvmField val guider: Guider,
) : SimpleJob(), PublishSubscribe<MessageEvent> {

    private val cameraCaptureEventHandler = CameraCaptureEventHandler(this)

    override val subject = PublishSubject.create<MessageEvent>()

    @Synchronized
    fun initialize() {
        clear()

        val initialDelayStep = DelayStep(plan.initialDelay)
        // initialDelayStep.registerDelayStepListener(cameraCaptureEventHandler)
        add(initialDelayStep)

        val waitForSettleStep = WaitForSettleStep(guider)

        fun mapRequest(request: CameraStartCaptureRequest): CameraStartCaptureRequest {
            return request.copy(savePath = plan.savePath, autoSave = true, autoSubFolderMode = AutoSubFolderMode.OFF)
        }

        if (plan.captureMode == SequenceCaptureMode.FULLY) {
            for (i in plan.slots.indices) {
                val request = mapRequest(plan.slots[i])
                val cameraExposureStep = CameraExposureStep(request)
                val delayStep = DelayStep(request.exposureDelay)
                val delayAndWaitForSettleStep = SimpleSplitStep(waitForSettleStep, delayStep)
                val ditherStep = DitherAfterExposureStep(request.dither, guider)

                add(SequenceIdStep(i + 1))

                repeat(request.exposureAmount) {
                    if (i == 0 && it == 0) add(waitForSettleStep)
                    else add(delayAndWaitForSettleStep)
                    add(cameraExposureStep)
                    add(ditherStep)
                }

                cameraExposureStep.registerCameraCaptureListener(cameraCaptureEventHandler)
            }
        } else {
            val requests = plan.slots.map(::mapRequest)
            val count = IntArray(requests.size)
            val delaySteps = requests.map { DelayStep(it.exposureDelay) }
            val ditherSteps = requests.map { DitherAfterExposureStep(it.dither, guider) }
            val cameraExposureSteps = requests.map { CameraExposureStep(it) }
            val delayAndWaitForSettleSteps = requests.indices.map { SimpleSplitStep(waitForSettleStep, delaySteps[it]) }
            val sequenceIdSteps = requests.indices.map { SequenceIdStep(it + 1) }

            while (true) {
                var added = false

                for (i in plan.slots.indices) {
                    val request = requests[i]

                    if (count[i] < request.exposureAmount) {
                        add(sequenceIdSteps[i])

                        if (i == 0 && count[i] == 0) add(waitForSettleStep)
                        else add(delayAndWaitForSettleSteps[i])

                        add(cameraExposureSteps[i])
                        add(ditherSteps[i])

                        count[i]++
                        added = true
                    }
                }

                if (!added) break
            }

            cameraExposureSteps.forEach { it.registerCameraCaptureListener(cameraCaptureEventHandler) }
        }
    }

    override fun onNext(event: MessageEvent) {
        if (event is CameraCaptureEvent) {
            val id = event.jobExecution.context.getInt(SequenceIdStep.ID)
            super.onNext(SequencerEvent(id, event))
        } else {
            println(event)
        }
    }

    private data class SequenceIdStep(private val id: Int) : Step {

        override fun execute(stepExecution: StepExecution): StepResult {
            stepExecution.context[ID] = id
            return StepResult.FINISHED
        }

        companion object {

            const val ID = "SEQUENCE.ID"
        }
    }
}
