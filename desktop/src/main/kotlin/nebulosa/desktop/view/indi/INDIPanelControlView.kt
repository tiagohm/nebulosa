package nebulosa.desktop.view.indi

import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.view.View
import nebulosa.indi.device.Device
import nebulosa.indi.device.PropertyVector

interface INDIPanelControlView : View {

    val device: Device?

    fun show(device: Device)

    fun updateLog(text: String)

    fun clearTabs()

    // TODO: Remove the strong reference to INDIPanelControlWindow
    fun makeGroup(name: String, vectors: List<PropertyVector<*, *>>): INDIPanelControlWindow.Group
}
