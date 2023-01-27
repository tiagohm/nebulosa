package nebulosa.desktop.logic.telescopecontrol

enum class TelescopeControlServerType(val type: Class<out TelescopeControlServer>) {
    STELLARIUM(TelescopeControlTCPServer::class.java),
    LX200(TelescopeControlLX200Server::class.java),
}
