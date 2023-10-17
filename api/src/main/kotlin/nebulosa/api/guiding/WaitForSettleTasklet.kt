package nebulosa.api.guiding

import nebulosa.guiding.Guider
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired

class WaitForSettleTasklet : StoppableTasklet {

    @Autowired private lateinit var guider: Guider

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        if (guider.isSettling) {
            guider.waitForSettle()
        }

        return RepeatStatus.FINISHED
    }

    override fun stop() {
    }
}
