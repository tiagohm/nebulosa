package nebulosa.api.sequencer

import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class SequencerService(
    private val sequencesPath: Path,
    private val sequencerExecutor: SequencerExecutor,
) {

    @Synchronized
    fun startSequencer(request: SequencePlanRequest) {
        if (sequencerExecutor.isRunning()) return

        val savePath = request.savePath
            ?.takeIf { "$it".isNotBlank() && it.exists() && it.isDirectory() }
            ?: Path.of("$sequencesPath", (System.currentTimeMillis() / 1000).toString())

        sequencerExecutor
            .execute(request.copy(savePath = savePath))
    }

    fun stopSequencer() {
        sequencerExecutor.stop()
    }
}
