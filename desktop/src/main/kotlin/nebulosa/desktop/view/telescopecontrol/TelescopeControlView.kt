package nebulosa.desktop.view.telescopecontrol

import nebulosa.desktop.view.View

interface TelescopeControlView : View {

    val type: TelescopeControlType

    val host: String

    val port: Int

    fun updateConnectionStatus(connected: Boolean, host: String, port: Int)
}
