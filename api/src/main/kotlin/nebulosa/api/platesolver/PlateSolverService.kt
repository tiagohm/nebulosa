package nebulosa.api.platesolver

import nebulosa.api.image.ImageBucket
import nebulosa.api.image.ImageSolved
import nebulosa.platesolver.PlateSolution
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

class PlateSolverService(
    private val imageBucket: ImageBucket,
    private val executor: ExecutorService,
) {

    private val tasks = ConcurrentHashMap<String, Future<PlateSolution>>(4)

    fun start(request: PlateSolverRequest, path: Path, key: String): ImageSolved {
        val calibration = try {
            solve(request, path, key).get()
        } catch (e: CancellationException) {
            return ImageSolved.NO_SOLUTION
        } catch (e: Throwable) {
            throw RuntimeException(e.message)
        }

        imageBucket.put(path, calibration)

        if (!calibration.solved) {
            throw RuntimeException("no solution found!")
        }

        return ImageSolved(calibration)
    }

    fun solve(request: PlateSolverRequest, path: Path, key: String): Future<PlateSolution> {
        val solver = request.get()
        val radius = if (request.blind) 0.0 else request.radius

        val task = FutureTask { solver.solve(path, null, request.centerRA, request.centerDEC, radius, request.downsampleFactor, request.timeout) }

        tasks[key] = task
        executor.submit(task)
        return task
    }

    fun stop(key: String) {
        tasks.remove(key)?.cancel(true)
    }
}
