package nebulosa.api.platesolver

import nebulosa.api.image.ImageBucket
import nebulosa.api.image.ImageSolved
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.math.Angle
import okhttp3.OkHttpClient
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

@Service
class PlateSolverService(
    private val imageBucket: ImageBucket,
    private val httpClient: OkHttpClient,
) {

    private val cancellationToken = AtomicReference<CancellationToken>()

    fun solveImage(
        options: PlateSolverRequest, path: Path,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
    ): ImageSolved {
        val calibration = solve(options, path, centerRA, centerDEC, radius)
        imageBucket.put(path, calibration)
        return ImageSolved(calibration)
    }

    @Synchronized
    fun solve(
        options: PlateSolverRequest, path: Path,
        centerRA: Angle = 0.0, centerDEC: Angle = 0.0, radius: Angle = 0.0,
    ) = CancellationToken().use {
        cancellationToken.set(it)
        options.get(httpClient).solve(path, null, centerRA, centerDEC, radius, options.downsampleFactor, options.timeout, it)
    }

    fun stopSolver() {
        cancellationToken.get()?.cancel()
    }
}
