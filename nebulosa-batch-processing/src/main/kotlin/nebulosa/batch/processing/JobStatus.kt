package nebulosa.batch.processing

enum class JobStatus {
    STARTING,
    STARTED,
    STOPPING,
    STOPPED,
    PAUSED,
    FAILED,
    COMPLETED,
    ABANDONED,
}
