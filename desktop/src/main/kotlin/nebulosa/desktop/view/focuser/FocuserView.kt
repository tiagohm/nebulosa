package nebulosa.desktop.view.focuser

import nebulosa.desktop.view.View

interface FocuserView : View {

    val status: String

    val increment: Int

    val maxIncrement: Int

    val absolute: Int

    val absoluteMax: Int

    suspend fun updateStatus(status: String)

    suspend fun updateMaxIncrement(value: Int)

    suspend fun updateAbsoluteMax(value: Int)
}
