package nebulosa.desktop.gui

import java.io.Closeable

interface View : Closeable {

    var isResizable: Boolean

    var isMaximized: Boolean

    val isShowing: Boolean

    var title: String

    var x: Double

    var y: Double

    var width: Double

    var height: Double

    val sceneWidth: Double

    val sceneHeight: Double

    val borderSize: Double

    val titleHeight: Double

    fun show(requestFocus: Boolean = false, bringToFront: Boolean = false)

    fun showAlert(message: String, title: String = "Information")
}
