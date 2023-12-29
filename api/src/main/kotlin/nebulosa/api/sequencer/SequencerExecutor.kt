package nebulosa.api.sequencer

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.messages.MessageEvent
import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.JobLauncher
import nebulosa.guiding.Guider
import nebulosa.log.debug
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component
import java.util.*

@Component
class SequencerExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
    private val jobLauncher: JobLauncher,
) : Consumer<MessageEvent> {

    private val jobExecutions = LinkedList<JobExecution>()

    @Synchronized
    fun execute(request: SequencePlanRequest) {
        check(!isRunning()) { "job is already running" }

        LOG.debug { "starting sequencer. request=%s".format(request) }

        val sequencerJob = SequencerJob(request, guider)
        sequencerJob.subscribe(this)
        sequencerJob.initialize()
        jobExecutions.add(jobLauncher.launch(sequencerJob))
    }

    fun findJobExecution(): JobExecution? {
        return jobExecutions.lastOrNull { !it.isDone }
    }

    fun isRunning(): Boolean {
        return findJobExecution() != null
    }

    @Synchronized
    fun stop() {
        val jobExecution = findJobExecution() ?: return
        jobLauncher.stop(jobExecution)
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<SequencerExecutor>()
    }
}
