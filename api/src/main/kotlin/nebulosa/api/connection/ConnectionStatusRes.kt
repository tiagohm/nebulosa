package nebulosa.api.connection

data class ConnectionStatusRes(
    val connected: Boolean,
    val host: String,
    val port: Int,
)
