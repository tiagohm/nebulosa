package nebulosa.batch.processing

enum class JobStatus {
    STARTING,
    STARTED,
    STOPPING,
    STOPPED,
    PAUSING,
    PAUSED,
    FAILED,
    COMPLETED,
    ABANDONED,
}
