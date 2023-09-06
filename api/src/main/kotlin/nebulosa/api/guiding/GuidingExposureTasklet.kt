package nebulosa.api.guiding

import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus

class GuidingExposureTasklet(private val camera: Camera) : StoppableTasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        return RepeatStatus.FINISHED
    }

    override fun stop() {

    }
}
