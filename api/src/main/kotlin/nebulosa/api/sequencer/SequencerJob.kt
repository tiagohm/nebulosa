package nebulosa.api.sequencer

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.*
import nebulosa.api.focusers.FocusOffsetStep
import nebulosa.api.messages.MessageEvent
import nebulosa.api.wheels.WheelStep
import nebulosa.batch.processing.*
import nebulosa.batch.processing.ExecutionContext.Companion.getDuration
import nebulosa.batch.processing.ExecutionContext.Companion.getInt
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import java.time.Duration
import java.time.LocalDateTime

// https://cdn.diffractionlimited.com/help/maximdl/Autosave_Sequence.htm
// https://nighttime-imaging.eu/docs/master/site/tabs/sequence/
// https://nighttime-imaging.eu/docs/master/site/sequencer/advanced/advanced/

data class SequencerJob(
    @JvmField val camera: Camera,
    @JvmField val plan: SequencePlanRequest,
    @JvmField val guider: Guider,
    @JvmField val wheel: FilterWheel? = null,
    @JvmField val focuser: Focuser? = null,
) : SimpleJob(), PublishSubscribe<MessageEvent> {

    // private val cameraCaptureEventHandler = CameraCaptureEventHandler(this)

    @Volatile var estimatedCaptureTime = plan.initialDelay
        private set

    override val subject = PublishSubject.create<MessageEvent>()

    @Synchronized
    fun initialize() {
        clear()

        val initialDelayStep = DelayStep(plan.initialDelay)
        initialDelayStep.registerDelayStepListener(this)
        register(initialDelayStep)

        val waitForSettleStep = WaitForSettleStep(guider)

        fun mapRequest(request: CameraStartCaptureRequest): CameraStartCaptureRequest {
            return request.copy(savePath = plan.savePath, autoSave = true, autoSubFolderMode = plan.autoSubFolderMode)
        }

        fun CameraStartCaptureRequest.wheelStep(): Step? {
            return if (wheel != null) WheelStep(wheel, if (frameType == FrameType.DARK) shutterPosition else filterPosition) else null
        }

        fun CameraStartCaptureRequest.focusStep(): Step? {
            return if (focuser != null) FocusOffsetStep(focuser, focusOffset) else null
        }

        val usedEntries = plan.entries.filter { it.enabled }

        require(usedEntries.isNotEmpty()) { "no entries found" }

        if (plan.captureMode == SequenceCaptureMode.FULLY || usedEntries.size == 1) {
            for (i in usedEntries.indices) {
                val request = mapRequest(usedEntries[i])
                val cameraExposureStep = CameraExposureStep(camera, request)
                val delayStep = DelayStep(request.exposureDelay)
                delayStep.registerDelayStepListener(cameraExposureStep)
                val delayAndWaitForSettleStep = SimpleSplitStep(waitForSettleStep, delayStep)
                val ditherStep = DitherAfterExposureStep(request.dither, guider)
                val wheelStep = request.wheelStep()
                val focusStep = request.focusStep()
                var estimatedCaptureTimeForEntry = Duration.ZERO

                register(SequenceIdStep(plan.entries.indexOf(usedEntries[i]) + 1))

                repeat(request.exposureAmount) {
                    if (i == 0 && it == 0) {
                        register(waitForSettleStep)
                    } else {
                        register(delayAndWaitForSettleStep)
                        estimatedCaptureTime += request.exposureDelay
                        estimatedCaptureTimeForEntry += request.exposureDelay
                    }

                    wheelStep?.also(::register)
                    focusStep?.also(::register)
                    register(cameraExposureStep)
                    register(ditherStep)

                    estimatedCaptureTime += request.exposureTime
                    estimatedCaptureTimeForEntry += request.exposureTime
                }

                // cameraExposureStep.registerCameraCaptureListener(cameraCaptureEventHandler)
                cameraExposureStep.estimatedCaptureTime = estimatedCaptureTimeForEntry
            }
        } else {
            val sequenceIdSteps = usedEntries.map { SequenceIdStep(plan.entries.indexOf(it) + 1) }
            val requests = usedEntries.map(::mapRequest)
            val count = IntArray(requests.size)
            val delaySteps = requests.map { DelayStep(it.exposureDelay) }
            val ditherSteps = requests.map { DitherAfterExposureStep(it.dither, guider) }
            val cameraExposureSteps = requests.map { CameraExposureStep(camera, it) }
            delaySteps.indices.forEach { delaySteps[it].registerDelayStepListener(cameraExposureSteps[it]) }
            val delayAndWaitForSettleSteps = requests.indices.map { SimpleSplitStep(waitForSettleStep, delaySteps[it]) }
            val wheelSteps = requests.map { it.wheelStep() }
            val focusSteps = requests.map { it.focusStep() }
            val estimatedCaptureTimeForEntry = Array(requests.size) { Duration.ZERO }

            while (true) {
                var added = false

                for (i in usedEntries.indices) {
                    val request = requests[i]

                    if (count[i] < request.exposureAmount) {
                        register(sequenceIdSteps[i])

                        if (i == 0 && count[i] == 0) {
                            register(waitForSettleStep)
                        } else {
                            register(delayAndWaitForSettleSteps[i])
                            estimatedCaptureTime += delaySteps[i].duration
                            estimatedCaptureTimeForEntry[i] += delaySteps[i].duration
                        }

                        wheelSteps[i]?.also(::register)
                        focusSteps[i]?.also(::register)
                        register(cameraExposureSteps[i])
                        register(ditherSteps[i])

                        estimatedCaptureTime += cameraExposureSteps[i].exposureTime
                        estimatedCaptureTimeForEntry[i] += cameraExposureSteps[i].exposureTime

                        count[i]++
                        added = true
                    }
                }

                if (!added) break
            }

            // cameraExposureSteps.forEach { it.registerCameraCaptureListener(cameraCaptureEventHandler) }
            estimatedCaptureTimeForEntry.indices.forEach { cameraExposureSteps[it].estimatedCaptureTime = estimatedCaptureTimeForEntry[it] }
        }
    }

    override fun beforeJob(jobExecution: JobExecution) {
        jobExecution.context[STARTED_AT] = LocalDateTime.now()
    }

    override fun afterJob(jobExecution: JobExecution) {
        val id = jobExecution.context.getInt(SequenceIdStep.ID)
        super.onNext(SequencerElapsed(id, estimatedCaptureTime, Duration.ZERO, 1.0))
    }

    override fun onNext(event: MessageEvent) {
//        if (event is CameraCaptureElapsed) {
//            val context = event.jobExecution.context
//            val id = context.getInt(SequenceIdStep.ID)
//
//            context["$ELAPSED_TIME.$id"] = event.captureElapsedTime
//            context["$REMAINING_TIME.$id"] = event.captureRemainingTime
//            context["$PROGRESS.$id"] = event.captureProgress
//
//            var elapsedTime = plan.initialDelay
//
//            for (i in 1..32) {
//                elapsedTime += context.getDurationOrNull("$ELAPSED_TIME.$i") ?: break
//            }
//
//            val progress = elapsedTime.toMillis() / estimatedCaptureTime.toMillis().toDouble()
//
//            super.onNext(SequencerElapsed(id, elapsedTime, estimatedCaptureTime - elapsedTime, progress, event))
//        }
//
//        if (event is CameraExposureFinished) {
//            super.onNext(event)
//        }
    }

    override fun onDelayElapsed(step: DelayStep, stepExecution: StepExecution) {
        val context = stepExecution.jobExecution.context
        val remainingTime = context.getDuration(DelayStep.REMAINING_TIME)
        val elapsedTime = step.duration - remainingTime
        val progress = elapsedTime.toMillis() / estimatedCaptureTime.toMillis().toDouble()

        super.onNext(SequencerElapsed(0, elapsedTime, estimatedCaptureTime - elapsedTime, progress))
    }

    override fun contains(data: Any): Boolean {
        return data === camera || data === focuser || data === wheel || super.contains(data)
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
        const val ELAPSED_TIME = "SEQUENCER.ELAPSED_TIME"
        const val REMAINING_TIME = "SEQUENCER.REMAINING_TIME"
        const val PROGRESS = "SEQUENCER.PROGRESS"
    }
}
