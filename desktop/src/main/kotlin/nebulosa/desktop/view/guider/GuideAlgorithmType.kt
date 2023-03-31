package nebulosa.desktop.view.guider

enum class GuideAlgorithmType(@JvmField val label: String) {
    IDENTITY("Identity"),
    HYSTERESIS("Hysteresis"),
    LOW_PASS("Low Pass"),
    LOW_PASS_2("Low Pass II"),
    RESIST_SWITCH("Resist Switch"),
}
