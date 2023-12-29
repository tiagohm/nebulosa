package nebulosa.common.concurrency

import java.util.function.Consumer

fun interface CancellationListener : Consumer<CancellationSource>
