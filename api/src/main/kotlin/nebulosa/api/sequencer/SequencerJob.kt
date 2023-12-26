package nebulosa.api.sequencer

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.*
import nebulosa.api.focusers.FocusOffsetStep
import nebulosa.api.guiding.DitherAfterExposureStep
import nebulosa.api.guiding.WaitForSettleStep
import nebulosa.api.messages.MessageEvent
import nebulosa.api.wheels.WheelStep
import nebulosa.batch.processing.*
import nebulosa.batch.processing.ExecutionContext.Companion.getDouble
import nebulosa.batch.processing.ExecutionContext.Companion.getDuration
import nebulosa.batch.processing.ExecutionContext.Companion.getInt
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.FrameType
import java.time.LocalDateTime
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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

        fun CameraStartCaptureRequest.wheelStep(): Step? {
            return if (wheel != null) WheelStep(wheel, if (frameType == FrameType.DARK) shutterPosition else wheelPosition) else null
        }

        fun CameraStartCaptureRequest.focusStep(): Step? {
            return if (focuser != null) FocusOffsetStep(focuser, focusOffset) else null
        }

        val validEntries = plan.entries.filter { it.enabled }

        if (plan.captureMode == SequenceCaptureMode.FULLY) {
            for (i in validEntries.indices) {
                val request = mapRequest(validEntries[i])
                val cameraExposureStep = CameraExposureStep(request)
                val delayStep = DelayStep(request.exposureDelay)
                delayStep.registerDelayStepListener(cameraExposureStep)
                val delayAndWaitForSettleStep = SimpleSplitStep(waitForSettleStep, delayStep)
                val ditherStep = DitherAfterExposureStep(request.dither, guider)
                val wheelStep = request.wheelStep()
                val focusStep = request.focusStep()

                add(SequenceIdStep(plan.entries.indexOf(validEntries[i]) + 1))

                repeat(request.exposureAmount) {
                    if (i == 0 && it == 0) add(waitForSettleStep)
                    else add(delayAndWaitForSettleStep)
                    wheelStep?.also(::add)
                    focusStep?.also(::add)
                    add(cameraExposureStep)
                    add(ditherStep)
                }

                cameraExposureStep.registerCameraCaptureListener(cameraCaptureEventHandler)
            }
        } else {
            val sequenceIdSteps = validEntries.map { SequenceIdStep(plan.entries.indexOf(it) + 1) }
            val requests = validEntries.map(::mapRequest)
            val count = IntArray(requests.size)
            val delaySteps = requests.map { DelayStep(it.exposureDelay) }
            val ditherSteps = requests.map { DitherAfterExposureStep(it.dither, guider) }
            val cameraExposureSteps = requests.map { CameraExposureStep(it) }
            delaySteps.indices.forEach { delaySteps[it].registerDelayStepListener(cameraExposureSteps[it]) }
            val delayAndWaitForSettleSteps = requests.indices.map { SimpleSplitStep(waitForSettleStep, delaySteps[it]) }
            val wheelSteps = requests.map { it.wheelStep() }
            val focusSteps = requests.map { it.focusStep() }

            while (true) {
                var added = false

                for (i in validEntries.indices) {
                    val request = requests[i]

                    if (count[i] < request.exposureAmount) {
                        add(sequenceIdSteps[i])

                        if (i == 0 && count[i] == 0) add(waitForSettleStep)
                        else add(delayAndWaitForSettleSteps[i])

                        wheelSteps[i]?.also(::add)
                        focusSteps[i]?.also(::add)
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

    override fun beforeJob(jobExecution: JobExecution) {
        val cameraSteps = filterIsInstance<CameraExposureStep>()
        val estimatedCaptureTime = cameraSteps.sumOf { it.estimatedCaptureTime.toMillis() }.toDuration(DurationUnit.MILLISECONDS)

        jobExecution.context[ESTIMATED_CAPTURE_TIME] = estimatedCaptureTime
        jobExecution.context[STARTED_AT] = LocalDateTime.now()
        jobExecution.context[STEP_COUNT] = cameraSteps.size
    }

    override fun onNext(event: MessageEvent) {
        if (event is CameraCaptureEvent) {
            val context = event.jobExecution.context
            val id = context.getInt(SequenceIdStep.ID)

            var elapsedTime = event.captureElapsedTime
            var remainingTime = event.captureRemainingTime
            var progress = event.captureProgress
            val stepCount = context.getInt(STEP_COUNT, 1)

            for (i in 1 until id) {
                elapsedTime += context.getDuration("$ELAPSED_TIME.$i")
                remainingTime += context.getDuration("$REMAINING_TIME.$i")
                progress += context.getDouble("$PROGRESS.$i")
            }

            context["$ELAPSED_TIME.$id"] = event.captureElapsedTime
            context["$REMAINING_TIME.$id"] = event.captureRemainingTime
            context["$PROGRESS.$id"] = event.captureProgress

            super.onNext(SequencerEvent(id, elapsedTime, remainingTime, progress / stepCount, event))
        }

        super.onNext(event)
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

    companion object {

        const val STARTED_AT = "SEQUENCER.STARTED_AT"
        const val STEP_COUNT = "SEQUENCER.STEP_COUNT"
        const val ESTIMATED_CAPTURE_TIME = "SEQUENCER.ESTIMATED_CAPTURE_TIME"
        const val ELAPSED_TIME = "SEQUENCER.ELAPSED_TIME"
        const val REMAINING_TIME = "SEQUENCER.REMAINING_TIME"
        const val PROGRESS = "SEQUENCER.PROGRESS"
    }
}
