package nebulosa.api.sequencer

enum class SequencerCaptureMode {
    INTERLEAVED,

    /**
     * Processes each sequence in full before advancing to the next sequence.
     */
    FULLY,
}
