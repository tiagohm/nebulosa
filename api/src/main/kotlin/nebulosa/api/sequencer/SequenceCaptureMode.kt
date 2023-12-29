package nebulosa.api.sequencer

enum class SequenceCaptureMode {
    INTERLEAVED,

    /**
     * Processes each sequence entry in full before advancing to the next sequence entry.
     */
    FULLY,
}
