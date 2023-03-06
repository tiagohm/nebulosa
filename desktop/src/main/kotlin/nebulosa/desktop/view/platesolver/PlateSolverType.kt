package nebulosa.desktop.view.platesolver

enum class PlateSolverType(private val label: String) {
    ASTROMETRY_NET_LOCAL("Astrometry.net (Local)"),
    ASTROMETRY_NET_ONLINE("Astrometry.net (Online)"),
    ASTAP("ASTAP"),
    WATNEY("Watney");

    override fun toString() = label
}
