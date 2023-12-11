package nebulosa.batch.processing

enum class BatchStatus {
    STARTING,
    STARTED,
    STOPPING,
    STOPPED,
    FAILED,
    COMPLETED,
    ABANDONED,
}
