package nebulosa.api.stacker

import jakarta.validation.Valid
import nebulosa.common.concurrency.cancel.CancellationToken
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

@Validated
@RestController
@RequestMapping("stacker")
class StackerController(
    private val stackerService: StackerService,
) {

    private val cancellationToken = AtomicReference<CancellationToken>()

    @PutMapping("start")
    fun start(@RequestBody @Valid body: StackingRequest): Path? {
        return if (cancellationToken.compareAndSet(null, CancellationToken())) {
            try {
                stackerService.stack(body, cancellationToken.get())
            } finally {
                cancellationToken.getAndSet(null)?.unlistenAll()
            }
        } else {
            null
        }
    }

    @GetMapping("running")
    fun isRunning(): Boolean {
        return cancellationToken.get() != null
    }

    @PutMapping("stop")
    fun stop() {
        cancellationToken.get()?.cancel()
    }

    @PutMapping("analyze")
    fun analyze(@RequestParam path: Path): AnalyzedTarget? {
        return stackerService.analyze(path)
    }
}
