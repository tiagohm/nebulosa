package nebulosa.desktop.view.focuser

import nebulosa.desktop.view.View

interface FocuserView : View {

    var status: String

    var increment: Int

    var maxIncrement: Int

    var absolute: Int

    var absoluteMax: Int
}
