package nebulosa.desktop.view

interface View {

    var resizable: Boolean

    var maximized: Boolean

    val showing: Boolean

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

    fun showAndWait()

    fun showAlert(message: String, title: String = "Information")

    fun hide()
}
