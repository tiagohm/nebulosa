package nebulosa.desktop.logic.guider

import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.view.guider.GuiderSettingsView
import nebulosa.desktop.view.guider.GuiderView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GuiderSettingsManager(@Autowired private val view: GuiderSettingsView) {

    @Autowired private lateinit var guiderView: GuiderView
    @Autowired private lateinit var preferences: Preferences

    fun save() {
        savePreferences()
    }

    fun loadPreferences() {
        val type = guiderView.type

        view.host = preferences.string("guiderSettings.$type.host") ?: "localhost"
        view.port = preferences.int("guiderSettings.$type.port") ?: 4400
        view.dither = preferences.double("guiderSettings.$type.dither") ?: 1.5
        view.ditherInRAOnly = preferences.bool("guiderSettings.$type.ditherInRAOnly")
        view.settlePixelTolerance = preferences.double("guiderSettings.$type.settlePixelTolerance") ?: 1.5
        view.minimumSettleTime = preferences.int("guiderSettings.$type.minumumSettleTime") ?: 12
        view.settleTimeout = preferences.int("guiderSettings.$type.settleTimeout") ?: 60
        view.guidingStartRetry = preferences.bool("guiderSettings.$type.guidingStartRetry")
        view.guidingStartTimeout = preferences.int("guiderSettings.$type.guidingStartTimeout") ?: 60
        view.roiPercentageToFindGuideStar = preferences.double("guiderSettings.$type.roiPercentageToFindGuideStar") ?: 100.0
    }

    fun savePreferences() {
        if (!view.initialized) return

        val type = guiderView.type

        preferences.string("guiderSettings.$type.host", view.host)
        preferences.int("guiderSettings.$type.port", view.port)
        preferences.double("guiderSettings.$type.dither", view.dither)
        preferences.bool("guiderSettings.$type.ditherInRAOnly", view.ditherInRAOnly)
        preferences.double("guiderSettings.$type.settlePixelTolerance", view.settlePixelTolerance)
        preferences.int("guiderSettings.$type.minumumSettleTime", view.minimumSettleTime)
        preferences.int("guiderSettings.$type.settleTimeout", view.settleTimeout)
        preferences.bool("guiderSettings.$type.guidingStartRetry", view.guidingStartRetry)
        preferences.int("guiderSettings.$type.guidingStartTimeout", view.guidingStartTimeout)
        preferences.double("guiderSettings.$type.roiPercentageToFindGuideStar", view.roiPercentageToFindGuideStar)
    }
}
