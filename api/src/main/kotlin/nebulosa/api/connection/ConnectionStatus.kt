package nebulosa.api.connection

data class ConnectionStatus(
    val connected: Boolean,
    val host: String,
    val port: Int,
)
