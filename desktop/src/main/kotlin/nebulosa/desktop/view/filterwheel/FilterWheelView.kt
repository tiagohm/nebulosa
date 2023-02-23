package nebulosa.desktop.view.filterwheel

import nebulosa.desktop.view.View

interface FilterWheelView : View {

    val status: String

    val useFilterWheelAsShutter: Boolean

    val filterAsShutter: Int

    val compactMode: Boolean

    fun updateStatus(status: String)

    fun useCompactMode(enable: Boolean)

    fun updateFilterNames(
        names: List<String>, useFilterWheelAsShutter: Boolean,
        filterAsShutter: Int, position: Int
    )
}
