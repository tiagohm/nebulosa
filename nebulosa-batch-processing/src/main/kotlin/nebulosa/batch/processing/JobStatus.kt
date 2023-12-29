package nebulosa.batch.processing

enum class JobStatus {
    STARTING,
    STARTED,
    STOPPING,
    STOPPED,
    FAILED,
    COMPLETED,
    ABANDONED,
}
