package nebulosa.job.manager

interface TimedTaskEvent : TaskEvent {

    val elapsedTime: Long

    val remainingTime: Long

    val progress: Double
}
