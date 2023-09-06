package nebulosa.api.guiding

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus

class GuidingTasklet : StoppableTasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        return RepeatStatus.CONTINUABLE
    }

    override fun stop() {

    }
}
