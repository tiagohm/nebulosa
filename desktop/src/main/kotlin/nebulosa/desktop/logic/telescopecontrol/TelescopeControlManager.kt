package nebulosa.desktop.logic.telescopecontrol

import nebulosa.desktop.view.telescopecontrol.TelescopeControlView

class TelescopeControlManager(private val view: TelescopeControlView) {

    fun updateConnectionStatus() {
        val server = TelescopeControlServer.SERVERS[view.type]!!
        view.updateConnectionStatus(server.running, server.host, server.port)
    }

    fun connect() {
        val server = TelescopeControlServer.SERVERS[view.type]!!

        if (server.running) {
            server.close()
        } else {
            server.start(view.host, view.port)
        }

        updateConnectionStatus()
    }
}
