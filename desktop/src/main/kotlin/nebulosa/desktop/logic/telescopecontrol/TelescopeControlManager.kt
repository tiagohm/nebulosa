package nebulosa.desktop.logic.telescopecontrol

import nebulosa.desktop.view.telescopecontrol.TelescopeControlView

class TelescopeControlManager(private val view: TelescopeControlView) {

    fun updateConnectionStatus() {
        val server = if (view.type == TelescopeControlServerType.LX200) TelescopeControlLX200Server
        else TelescopeControlStellariumServer
        view.updateConnectionStatus(server.running, server.host, server.port)
    }

    fun connect() {
        val server = if (view.type == TelescopeControlServerType.LX200) TelescopeControlLX200Server
        else TelescopeControlStellariumServer

        if (server.running) {
            server.close()
        } else {
            server.start(view.host, view.port)
        }

        updateConnectionStatus()
    }
}
