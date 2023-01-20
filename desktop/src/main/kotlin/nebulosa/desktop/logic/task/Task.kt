package nebulosa.desktop.logic.task

import java.util.concurrent.Callable

interface Task : Callable<Any> {

    fun closeGracefully()
}
