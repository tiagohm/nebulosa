package nebulosa.api.connection

data class ConnectionStatus(
    val id: String,
    val type: ConnectionType,
    val host: String, val port: Int,
)
