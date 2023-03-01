package nebulosa.desktop.view.guider

import nebulosa.desktop.view.View

interface GuiderSettingsView : View {

    var host: String

    var port: Int

    var dither: Double

    var ditherInRAOnly: Boolean

    var settlePixelTolerance: Double

    var minimumSettleTime: Int

    var settleTimeout: Int

    var guidingStartRetry: Boolean

    var guidingStartTimeout: Int

    var roiPercentageToFindGuideStar: Double
}
