package nebulosa.batch.processing

open class SimpleSplitStep : SplitStep, ArrayList<Step> {

    constructor(initialCapacity: Int = 4) : super(initialCapacity)

    constructor(steps: Collection<Step>) : super(steps)

    constructor(vararg steps: Step) : this(steps.toList())
}
