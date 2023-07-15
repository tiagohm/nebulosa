package nebulosa.api.components.loaders

import jakarta.annotation.PostConstruct
import java.util.concurrent.ExecutorService

sealed class Loader : Runnable {

    abstract val systemExecutorService: ExecutorService

    @PostConstruct
    private fun initialize() {
        systemExecutorService.submit(this)
    }
}
