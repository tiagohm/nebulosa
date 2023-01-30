package nebulosa.desktop.logic.telescopecontrol

enum class TelescopeControlServerType(val label: String) {
    STELLARIUM("Stellarium (JNow)"),
    LX200("LX200");

    override fun toString() = label
}
