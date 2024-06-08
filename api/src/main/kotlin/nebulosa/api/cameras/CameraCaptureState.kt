package nebulosa.api.cameras

enum class CameraCaptureState {
    IDLE,
    CAPTURE_STARTED,
    EXPOSURE_STARTED,
    EXPOSURING,
    WAITING,
    SETTLING,
    DITHERING,
    PAUSING,
    PAUSED,
    EXPOSURE_FINISHED,
    CAPTURE_FINISHED,
}
