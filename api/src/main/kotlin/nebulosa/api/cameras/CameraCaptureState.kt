package nebulosa.api.cameras

enum class CameraCaptureState {
    CAPTURE_STARTED,
    EXPOSURE_STARTED,
    EXPOSURING,
    WAITING,
    SETTLING,
    EXPOSURE_FINISHED,
    CAPTURE_FINISHED,
}
