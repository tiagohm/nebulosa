package nebulosa.nova.astrometry

import nebulosa.io.bufferedResource
import nebulosa.io.readDoubleArrayLe
import nebulosa.math.normalized
import nebulosa.math.toDegrees
import nebulosa.math.toHours
import nebulosa.nova.position.ICRF
import nebulosa.time.TT
import nebulosa.time.TimeBesselianEpoch

enum class Constellation(
    val latinName: String,
    val iau: String,
    val genitive: String,
    val description: String,
) {

    /**
     * Andromeda.
     */
    AND("Andromeda", "And", "Andromedae", "Princess of Ethiopia"),

    /**
     * Antlia.
     */
    ANT("Antlia", "Ant", "Antliae", "Air pump"),

    /**
     * Apus.
     */
    APS("Apus", "Aps", "Apodis", "Bird of Paradise"),

    /**
     * Aquila.
     */
    AQL("Aquila", "Aql", "Aquilae", "Eagle"),

    /**
     * Aquarius.
     */
    AQR("Aquarius", "Aqr", "Aquarii", "Water bearer"),

    /**
     * Ara.
     */
    ARA("Ara", "Ara", "Arae", "Altar"),

    /**
     * Aries.
     */
    ARI("Aries", "Ari", "Arietis", "Ram"),

    /**
     * Auriga.
     */
    AUR("Auriga", "Aur", "Aurigae", "Charioteer"),

    /**
     * Boötes.
     */
    BOO("Boötes", "Boo", "Boötis", "Herdsman"),

    /**
     * Canis Major.
     */
    CMA("Canis Major", "CMa", "Canis Majoris", "Big dog"),

    /**
     * Canis Minor.
     */
    CMI("Canis Minor", "CMi", "Canis Minoris", "Little dog"),

    /**
     * Canes Venatici.
     */
    CVN("Canes Venatici", "CVn", "Canum Venaticorum", "Hunting dogs"),

    /**
     * Caelum.
     */
    CAE("Caelum", "Cae", "Caeli", "Graving tool"),

    /**
     * Camelopardalis.
     */
    CAM("Camelopardalis", "Cam", "Camelopardalis", "Giraffe"),

    /**
     * Capricornus.
     */
    CAP("Capricornus", "Cap", "Capricorni", "Sea goat"),

    /**
     * Carina.
     */
    CAR("Carina", "Car", "Carinae", "Keel of Argonauts' ship"),

    /**
     * Cassiopeia.
     */
    CAS("Cassiopeia", "Cas", "Cassiopeiae", "Queen of Ethiopia"),

    /**
     * Centaurus.
     */
    CEN("Centaurus", "Cen", "Centauri", "Centaur"),

    /**
     * Cepheus.
     */
    CEP("Cepheus", "Cep", "Cephei", "King of Ethiopia"),

    /**
     * Cetus.
     */
    CET("Cetus", "Cet", "Ceti", "Sea monster (whale)"),

    /**
     * Chamaeleon.
     */
    CHA("Chamaeleon", "Cha", "Chamaeleontis", "Chameleon"),

    /**
     * Circinus.
     */
    CIR("Circinus", "Cir", "Circini", "Compasses"),

    /**
     * Cancer.
     */
    CNC("Cancer", "Cnc", "Cancri", "Crab"),

    /**
     * Columba.
     */
    COL("Columba", "Col", "Columbae", "Dove"),

    /**
     * Coma Berenices.
     */
    COM("Coma Berenices", "Com", "Comae Berenices", "Berenice's hair"),

    /**
     * Corona Australis.
     */
    CRA("Corona Australis", "CrA", "Coronae Australis", "Southern crown"),

    /**
     * Corona Borealis.
     */
    CRB("Corona Borealis", "CrB", "Coronae Borealis", "Northern crown"),

    /**
     * Crater.
     */
    CRT("Crater", "Crt", "Crateris", "Cup"),

    /**
     * Crux.
     */
    CRU("Crux", "Cru", "Crucis", "Cross"),

    /**
     * Corvus.
     */
    CRV("Corvus", "Crv", "Corvi", "Crow"),

    /**
     * Cygnus.
     */
    CYG("Cygnus", "Cyg", "Cygni", "Swan"),

    /**
     * Delphinus.
     */
    DEL("Delphinus", "Del", "Delphini", "Porpoise"),

    /**
     * Dorado.
     */
    DOR("Dorado", "Dor", "Doradus", "Swordfish"),

    /**
     * Draco.
     */
    DRA("Draco", "Dra", "Draconis", "Dragon"),

    /**
     * Equuleus.
     */
    EQU("Equuleus", "Equ", "Equulei", "Little horse"),

    /**
     * Eridanus.
     */
    ERI("Eridanus", "Eri", "Eridani", "River"),

    /**
     * Fornax.
     */
    FOR("Fornax", "For", "Fornacis", "Furnace"),

    /**
     * Gemini.
     */
    GEM("Gemini", "Gem", "Geminorum", "Twins"),

    /**
     * Grus.
     */
    GRU("Grus", "Gru", "Gruis", "Crane"),

    /**
     * Hercules.
     */
    HER("Hercules", "Her", "Herculis", "Hercules, son of Zeus"),

    /**
     * Horologium.
     */
    HOR("Horologium", "Hor", "Horologii", "Clock"),

    /**
     * Hydra.
     */
    HYA("Hydra", "Hya", "Hydrae", "Sea serpent"),

    /**
     * Hydrus.
     */
    HYI("Hydrus", "Hyi", "Hydri", "Water snake"),

    /**
     * Indus.
     */
    IND("Indus", "Ind", "Indi", "Indian"),

    /**
     * Leo Minor.
     */
    LMI("Leo Minor", "LMi", "Leonis Minoris", "Little lion"),

    /**
     * Lacerta.
     */
    LAC("Lacerta", "Lac", "Lacertae", "Lizard"),

    /**
     * Leo.
     */
    LEO("Leo", "Leo", "Leonis", "Lion"),

    /**
     * Lepus.
     */
    LEP("Lepus", "Lep", "Leporis", "Hare"),

    /**
     * Libra.
     */
    LIB("Libra", "Lib", "Librae", "Balance"),

    /**
     * Lupus.
     */
    LUP("Lupus", "Lup", "Lupi", "Wolf"),

    /**
     * Lynx.
     */
    LYN("Lynx", "Lyn", "Lyncis", "Lynx"),

    /**
     * Lyra.
     */
    LYR("Lyra", "Lyr", "Lyrae", "Lyre"),

    /**
     * Mensa.
     */
    MEN("Mensa", "Men", "Mensae", "Table mountain"),

    /**
     * Microscopium.
     */
    MIC("Microscopium", "Mic", "Microscopii", "Microscope"),

    /**
     * Monoceros.
     */
    MON("Monoceros", "Mon", "Monocerotis", "Unicorn"),

    /**
     * Musca.
     */
    MUS("Musca", "Mus", "Muscae", "Fly"),

    /**
     * Norma.
     */
    NOR("Norma", "Nor", "Normae", "Carpenter's Level"),

    /**
     * Octans.
     */
    OCT("Octans", "Oct", "Octantis", "Octant"),

    /**
     * Ophiuchus.
     */
    OPH("Ophiuchus", "Oph", "Ophiuchi", "Holder of serpent"),

    /**
     * Orion.
     */
    ORI("Orion", "Ori", "Orionis", "Orion, the hunter"),

    /**
     * Pavo.
     */
    PAV("Pavo", "Pav", "Pavonis", "Peacock"),

    /**
     * Pegasus.
     */
    PEG("Pegasus", "Peg", "Pegasi", "Pegasus, the winged horse"),

    /**
     * Perseus.
     */
    PER("Perseus", "Per", "Persei", "Perseus, hero who saved Andromeda"),

    /**
     * Phoenix.
     */
    PHE("Phoenix", "Phe", "Phoenicis", "Phoenix"),

    /**
     * Pictor.
     */
    PIC("Pictor", "Pic", "Pictoris", "Easel"),

    /**
     * Piscis Austrinus.
     */
    PSA("Piscis Austrinus", "PsA", "Piscis Austrini", "Southern fish"),

    /**
     * Pisces.
     */
    PSC("Pisces", "Psc", "Piscium", "Fishes"),

    /**
     * Puppis.
     */
    PUP("Puppis", "Pup", "Puppis", "Stern of the Argonauts' ship"),

    /**
     * Pyxis.
     */
    PYX("Pyxis", "Pyx", "Pyxidis", "Compass on the Argonauts' ship"),

    /**
     * Reticulum.
     */
    RET("Reticulum", "Ret", "Reticuli", "Net"),

    /**
     * Sculptor.
     */
    SCL("Sculptor", "Scl", "Sculptoris", "Sculptor's tools"),

    /**
     * Scorpius.
     */
    SCO("Scorpius", "Sco", "Scorpii", "Scorpion"),

    /**
     * Scutum.
     */
    SCT("Scutum", "Sct", "Scuti", "Shield"),

    /**
     * Serpens.
     */
    SER("Serpens", "Ser", "Serpentis", "Serpent"),

    /**
     * Sextans.
     */
    SEX("Sextans", "Sex", "Sextantis", "Sextant"),

    /**
     * Sagitta.
     */
    SGE("Sagitta", "Sge", "Sagittae", "Arrow"),

    /**
     * Sagittarius.
     */
    SGR("Sagittarius", "Sgr", "Sagittarii", "Archer"),

    /**
     * Taurus.
     */
    TAU("Taurus", "Tau", "Tauri", "Bull"),

    /**
     * Telescopium.
     */
    TEL("Telescopium", "Tel", "Telescopii", "Telescope"),

    /**
     * Triangulum Australe.
     */
    TRA("Triangulum Australe", "TrA", "Trianguli Australis", "Southern triangle"),

    /**
     * Triangulum.
     */
    TRI("Triangulum", "Tri", "Trianguli", "Triangle"),

    /**
     * Tucana.
     */
    TUC("Tucana", "Tuc", "Tucanae", "Toucan"),

    /**
     * Ursa Major.
     */
    UMA("Ursa Major", "UMa", "Ursae Majoris", "Big bear"),

    /**
     * Ursa Minor.
     */
    UMI("Ursa Minor", "UMi", "Ursae Minoris", "Little bear"),

    /**
     * Vela.
     */
    VEL("Vela", "Vel", "Velorum", "Sail of the Argonauts' ship"),

    /**
     * Virgo.
     */
    VIR("Virgo", "Vir", "Virginis", "Virgin"),

    /**
     * Volans.
     */
    VOL("Volans", "Vol", "Volantis", "Flying fish"),

    /**
     * Vulpecula.
     */
    VUL("Vulpecula", "Vul", "Vulpeculae", "Fox");

    companion object {

        @JvmStatic private val RA: DoubleArray
        @JvmStatic private val DEC: DoubleArray
        @JvmStatic private val RA_TO_INDEX: ByteArray
        @JvmStatic private val EPOCH = TT(TimeBesselianEpoch.B1875)

        init {
            bufferedResource("CONSTELLATIONS.dat")!!.use {
                RA = it.readDoubleArrayLe(235)
                DEC = it.readDoubleArrayLe(199)
                RA_TO_INDEX = it.readByteArray(202 * 236)
            }
        }

        @JvmStatic
        fun find(position: ICRF): Constellation {
            val (ra, dec) = position.equatorialAtEpoch(EPOCH)
            val i = RA.binarySearch(ra.normalized.toHours).let { if (it < 0) -it - 1 else it }
            val j = DEC.binarySearch(dec.toDegrees).let { if (it < 0) -it - 1 else it }
            val k = RA_TO_INDEX[i * 202 + j].toInt() and 0xFF
            return Constellation.entries[k]
        }
    }
}
