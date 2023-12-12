package nebulosa.batch.processing

abstract class SimpleFlowStep : FlowStep {

    protected abstract val steps: Collection<Step>

    override fun iterator(): Iterator<Step> {
        return steps.iterator()
    }
}
