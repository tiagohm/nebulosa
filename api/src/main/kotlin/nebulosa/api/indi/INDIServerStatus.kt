package nebulosa.api.indi

data class INDIServerStatus(
    @JvmField val running: Boolean = false,
    @JvmField val pid: Long = 0L,
)
