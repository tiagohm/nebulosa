package nebulosa.desktop.view.filterwheel

import nebulosa.desktop.view.View

interface FilterWheelView : View {

    var status: String

    var useFilterWheelAsShutter: Boolean

    var filterAsShutter: Int

    var compactMode: Boolean

    fun updateFilterNames(names: List<String>, selectedFilterAsShutter: Int, position: Int)
}
