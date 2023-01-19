package nebulosa.desktop.logic.taskexecutor

import java.util.concurrent.Callable

interface Task : Callable<Any> {

    fun closeGracefully()
}
