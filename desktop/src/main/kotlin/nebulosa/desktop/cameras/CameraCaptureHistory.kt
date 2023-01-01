package nebulosa.desktop.cameras

data class CameraCaptureHistory(
    val id: Long,
    val name: String,
    val path: String,
    val capturedAt: Long,
)
