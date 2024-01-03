import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import nebulosa.batch.processing.*
import nebulosa.log.loggerFor
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class BatchProcessingTest : StringSpec() {

    init {
        val launcher = AsyncJobLauncher(Executors.newSingleThreadExecutor())

        "single" {
            val jobExecution = launcher.launch(MathJob(listOf(SumStep())))
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe 1.0
        }
        "multiple" {
            val jobExecution = launcher.launch(MathJob(listOf(SumStep(), SumStep())))
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe 2.0
        }
        "split" {
            val jobExecution = launcher.launch(MathJob(listOf(SplitSumStep())))
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe N.toDouble()
        }
        "flow" {
            val jobExecution = launcher.launch(MathJob(listOf(FlowSumStep())))
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe N.toDouble()
        }
        "split flow" {
            val jobExecution = launcher.launch(MathJob(listOf(SimpleSplitStep(FlowSumStep(), FlowSumStep()))))
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe (N * 2).toDouble()
        }
        "stop" {
            val jobExecution = launcher.launch(MathJob((0..7).map { SumStep() }))
            thread { Thread.sleep(5000); launcher.stop(jobExecution) }
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] as Double shouldBeGreaterThanOrEqual 3.0
            jobExecution.isStopped.shouldBeTrue()
        }
        "repeatable" {
            val jobExecution = launcher.launch(MathJob(listOf(SumStep()), 10.0))
            jobExecution.waitForCompletion()
            jobExecution.context["VALUE"] shouldBe 20.0
        }
    }

    private class MathJob(
        steps: List<Step>,
        private val initialValue: Double = 0.0,
    ) : SimpleJob(steps) {

        override fun beforeJob(jobExecution: JobExecution) {
            jobExecution.context["VALUE"] = initialValue
        }
    }

    private abstract class MathStep : Step {

        @Volatile private var running = false

        protected abstract fun compute(value: Double): Double

        final override fun execute(stepExecution: StepExecution): StepResult {
            var sleepCount = 0

            val jobExecution = stepExecution.jobExecution
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
            } else {
                println("stopped")
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

    private class SplitSumStep : SimpleSplitStep() {

        init {
            repeat(N) {
                add(SumStep())
            }
        }
    }

    private class FlowSumStep : SimpleFlowStep() {

        init {
            repeat(N) {
                add(SumStep())
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<BatchProcessingTest>()
        @JvmStatic private val N = Runtime.getRuntime().availableProcessors()
    }
}
