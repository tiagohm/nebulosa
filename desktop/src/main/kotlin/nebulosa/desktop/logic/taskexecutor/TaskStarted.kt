package nebulosa.desktop.logic.taskexecutor

data class TaskStarted(override val task: Task) : TaskEvent
