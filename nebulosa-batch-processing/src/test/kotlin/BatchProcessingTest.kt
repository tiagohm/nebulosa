import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import nebulosa.batch.processing.*
import nebulosa.common.concurrency.DaemonThreadFactory
import nebulosa.log.loggerFor
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class BatchProcessingTest : StringSpec() {

    init {
        val launcher = AsyncJobLauncher(Executors.newSingleThreadExecutor(DaemonThreadFactory))

        "single" {
            val startedAt = System.currentTimeMillis()
            val jobExecution = launcher.launch(MathJob(listOf(SumStep())))
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe 1.0
            (System.currentTimeMillis() - startedAt) shouldBeInRange (1000L..2000L)
        }
        "multiple" {
            val startedAt = System.currentTimeMillis()
            val jobExecution = launcher.launch(MathJob(listOf(SumStep(), SumStep())))
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe 2.0
            (System.currentTimeMillis() - startedAt) shouldBeInRange (2000L..3000L)
        }
        "flow" {
            val startedAt = System.currentTimeMillis()
            val jobExecution = launcher.launch(MathJob(listOf(MultipleSumStep())))
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe NUMBER_OF_PROCESSORS.toDouble()
            (System.currentTimeMillis() - startedAt) shouldBeInRange (1000L..2000L)
        }
        "stop" {
            val startedAt = System.currentTimeMillis()
            val jobExecution = launcher.launch(MathJob((0..7).map { SumStep() }))
            thread { Thread.sleep(4000); jobExecution.stop() }
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe 4.0
            jobExecution.isStopped.shouldBeTrue()
            (System.currentTimeMillis() - startedAt) shouldBeInRange (4000L..5000L)
        }
        "repeatable" {
            val startedAt = System.currentTimeMillis()
            val jobExecution = launcher.launch(MathJob(listOf(SumStep()), 10.0))
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe 20.0
            (System.currentTimeMillis() - startedAt) shouldBeInRange (10000L..11000L)
        }
    }

    private data class MathJob(
        override val steps: List<Step>,
        private val initialValue: Double = 0.0,
    ) : SimpleJob() {

        override val id = "Job.Math"

        override fun beforeJob(jobExecution: JobExecution) {
            jobExecution.context["VALUE"] = initialValue
        }
    }

    private abstract class MathStep : Step {

        @Volatile private var running = false

        protected abstract fun compute(value: Double): Double

        final override fun execute(jobExecution: JobExecution): StepResult {
            var sleepCount = 0

            running = jobExecution.canContinue

            while (running && sleepCount++ < 100) {
                Thread.sleep(10)
            }

            if (running) {
                synchronized(jobExecution) {
                    val value = jobExecution.context["VALUE"]!! as Double
                    LOG.info("executing ${javaClass.simpleName}: $value")
                    jobExecution.context["VALUE"] = compute(value)

                    if (value >= 10.0 && value < 19.0) {
                        return StepResult.CONTINUABLE
                    }
                }
            }

            return StepResult.FINISHED
        }

        override fun stop(mayInterruptIfRunning: Boolean) {
            running = false
        }
    }

    private class SumStep : MathStep() {

        override fun compute(value: Double): Double {
            return value + 1.0
        }
    }

    private class MultipleSumStep : SimpleFlowStep() {

        override val steps = (0 until NUMBER_OF_PROCESSORS).map { SumStep() }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<BatchProcessingTest>()
        @JvmStatic private val NUMBER_OF_PROCESSORS = Runtime.getRuntime().availableProcessors()
    }
}
