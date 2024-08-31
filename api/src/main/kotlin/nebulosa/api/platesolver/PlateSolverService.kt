package nebulosa.api.platesolver

import nebulosa.api.image.ImageBucket
import nebulosa.api.image.ImageSolved
import nebulosa.util.concurrency.cancellation.CancellationToken
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

    fun solveImage(request: PlateSolverRequest, path: Path): ImageSolved {
        val calibration = solve(request, path)
        imageBucket.put(path, calibration)
        return ImageSolved(calibration)
    }

    @Synchronized
    fun solve(request: PlateSolverRequest, path: Path) = CancellationToken().use {
        cancellationToken.set(it)
        val solver = request.get(httpClient)
        val radius = if (request.blind) 0.0 else request.radius
        solver.solve(path, null, request.centerRA, request.centerDEC, radius, request.downsampleFactor, request.timeout, it)
    }

    fun stopSolver() {
        cancellationToken.get()?.cancel()
    }
}
