package nebulosa.simbad

typealias SimbadCatalogNameProvider = MatchResult.() -> String

enum class SimbadCatalogType(
    val regex: Regex,
    val isStar: Boolean, val isDSO: Boolean = !isStar,
    private val provider: SimbadCatalogNameProvider,
) {
    NAMED("NAME\\s+(.*)", true, true, { groupValues[1].trim() }),
    STAR("\\*\\s+(.*)", true, false, { groupValues[1].trim() }),
    HD("HD\\s+(\\w*)", true, false, { "HD " + groupValues[1] }),
    HR("HR\\s+(\\w*)", true, false, { "HR " + groupValues[1] }),
    HIP("HIP\\s+(\\w*)", true, false, { "HIP " + groupValues[1] }),
    NGC("NGC\\s+(\\w{1,5})", true, true, { "NGC " + groupValues[1].uppercase() }),
    IC("IC\\s+(\\w{1,5})", true, true, { "IC " + groupValues[1].uppercase() }),
    GUM("GUM\\s+(\\d{1,4})", false, true, { "Gum " + groupValues[1] }),
    M("M\\s+(\\d{1,3})", false, true, { "M " + groupValues[1] }),
    BARNARD("Barnard\\s+(\\d{1,3})", false, true, { "B " + groupValues[1] }),
    LBN("LBN\\s+(\\d{1,4})", false, true, { "LBN " + groupValues[1] }),
    LDN("LDN\\s+(\\d{1,4})", false, true, { "LDN " + groupValues[1] }),
    RCW("RCW\\s+(\\d{1,4})", false, true, { "RCW " + groupValues[1] }),
    SH("SH\\s+2-(\\d{1,3})", false, true, { "SH 2-" + groupValues[1] }),
    CED("Ced\\s+(\\d{1,3})", false, true, { "Ced " + groupValues[1] }),
    UGC("UGC\\s+(\\d{1,5})", false, true, { "UGC " + groupValues[1] }),
    APG("APG\\s+(\\d{1,3})", false, true, { "Arp " + groupValues[1] }),
    HCG("HCG\\s+(\\d{1,3})", false, true, { "HCG " + groupValues[1] }),
    VV("VV\\s+(\\d{1,4})", false, true, { "VV " + groupValues[1] }),
    VDBH("VdBH\\s+(\\d{1,2})", false, true, { "VdBH " + groupValues[1] }),
    DWB("DWB\\s+(\\d{1,3})", false, true, { "DWB " + groupValues[1] }),
    LEDA("LEDA\\s+(\\d{1,7})", false, true, { "PGC " + groupValues[1] }),
    ACO("ACO\\s+(\\d{1,7})", false, true, { "ACO " + groupValues[1] }),
    SNR("SNR\\s+([\\w.\\-+]+)", false, true, { "SNR " + groupValues[1] }),
    ESO("ESO\\s+(\\d+-\\w+)", false, true, { "ESO " + groupValues[1] }),
    PK("PK\\s+(\\d{1,3}\\w\\d{1,2})\\s+(\\d{1,2})", false, true, { "PK " + groupValues[1] + "." + groupValues[2] }),
    CL("Cl\\s+([\\w\\-]+)\\s+(\\d{1,5})", false, true, {
        when (val name = groupValues[1]) {
            "Melotte" -> "Mel"
            "Stock" -> "St"
            "Ruprecht" -> "Ru"
            "Trumpler" -> "Tr"
            "Collinder" -> "Cr"
            else -> name
        } + " " + groupValues[2]
    });

    constructor(
        regex: String,
        isStar: Boolean, isDSO: Boolean = !isStar,
        provider: MatchResult.() -> String,
    ) : this(regex.toRegex(), isStar, isDSO, provider)

    fun matches(name: String) = regex.matches(name)

    fun match(name: String) = regex.matchEntire(name)?.let { provider(it) }

    companion object {

        @JvmStatic
        fun none(name: String) = entries.none { it.matches(name) }

        @JvmStatic
        fun any(name: String) = entries.any { it.matches(name) }
    }
}
