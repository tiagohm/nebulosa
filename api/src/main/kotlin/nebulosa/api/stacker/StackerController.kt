package nebulosa.api.stacker

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.javalin.exists
import nebulosa.api.javalin.notNull
import nebulosa.api.javalin.path
import nebulosa.api.javalin.valid
import nebulosa.util.concurrency.cancellation.CancellationToken
import java.util.concurrent.atomic.AtomicReference

class StackerController(
    app: Javalin,
    private val stackerService: StackerService,
) {

    private val cancellationToken = AtomicReference<CancellationToken>()

    init {
        app.put("stacker/start", ::start)
        app.get("stacker/running", ::isRunning)
        app.put("stacker/stop", ::stop)
        app.put("stacker/analyze", ::analyze)
    }

    private fun start(ctx: Context) {
        val body = ctx.bodyAsClass<StackingRequest>().valid()

        if (cancellationToken.compareAndSet(null, CancellationToken())) {
            try {
                stackerService.stack(body, cancellationToken.get())?.also(ctx::json)
            } finally {
                cancellationToken.getAndSet(null)?.unlistenAll()
            }
        }
    }

    private fun isRunning(ctx: Context) {
        ctx.json(cancellationToken.get() != null)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun stop(ctx: Context) {
        cancellationToken.get()?.cancel()
    }

    private fun analyze(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path().exists()
        stackerService.analyze(path)?.also(ctx::json)
    }
}
