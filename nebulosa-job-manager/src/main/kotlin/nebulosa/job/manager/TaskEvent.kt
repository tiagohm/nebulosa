package nebulosa.job.manager

interface TaskEvent {

    val job: Job

    val task: Task
}
