package nebulosa.skycatalog

enum class ClassificationType(private vararg val codes: String) {
    STAR("*"),
    SET_OF_STARS("Cl*", "As*"),
    INTERSTELLAR_MEDIUM("ISM"),
    GALAXY("G"),
    SET_OF_GALAXIES("IG", "PaG", "GrG", "ClG", "PCG", "SCG", "vid"),
    GRAVITATION("grv"),
    SPECTRAL("ev", "var", "Rad", "IR", "Opt", "UV", "X", "gam"),
    OTHER("mul", "err", "Poc", "PoG", "?", "reg"),
}
