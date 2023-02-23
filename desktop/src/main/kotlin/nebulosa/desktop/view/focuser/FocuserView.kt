package nebulosa.desktop.view.focuser

import nebulosa.desktop.view.View

interface FocuserView : View {

    val status: String

    val increment: Int

    val maxIncrement: Int

    val absolute: Int

    val absoluteMax: Int

    fun updateStatus(status: String)

    fun updateMaxIncrement(value: Int)

    fun updateAbsoluteMax(value: Int)
}
