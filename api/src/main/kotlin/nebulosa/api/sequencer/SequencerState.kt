package nebulosa.api.sequencer

enum class SequencerState {
    IDLE,
    WAITING,
    RUNNING,
    PAUSING,
    PAUSED,
}
