package nebulosa.api.connection

data class ConnectionStatus(
    @JvmField val id: String,
    @JvmField val type: ConnectionType,
    @JvmField val host: String,
    @JvmField val port: Int,
    @JvmField val ip: String? = null,
)
