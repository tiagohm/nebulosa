package nebulosa.phd2.client.commands

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

internal data class CompletableCommand<T>(
    @JvmField internal val command: PHD2Command<T>,
    @JvmField internal val task: CompletableFuture<T>,
    @JvmField internal val id: String,
) : PHD2Command<T> by command, Future<T> by task
