package nebulosa.desktop.logic.taskexecutor

interface Task : Runnable {

    fun closeGracefully()
}
