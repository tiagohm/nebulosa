package nebulosa.batch.processing

import java.util.concurrent.ConcurrentHashMap

open class ExecutionContext : ConcurrentHashMap<String, Any?> {

    constructor(initialCapacity: Int = 64) : super(initialCapacity)

    constructor(context: ExecutionContext) : super(context)
}
