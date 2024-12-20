package nebulosa.skycatalog

// https://vizier.cds.unistra.fr/cgi-bin/OType

enum class SkyObjectType(
    val description: String,
    val classification: ClassificationType,
    vararg val codes: String,
) {
    ACTIVE_GALAXY_NUCLEUS("Active Galaxy Nucleus", ClassificationType.GALAXY, "AGN", "AG?"),
    ALPHA2_CVN_VARIABLE("alpha2 CVn Variable", ClassificationType.STAR, "a2*", "a2?"),
    ASSOCIATION_OF_STARS("Association of Stars", ClassificationType.SET_OF_STARS, "As*", "As?"),
    ASYMPTOTIC_GIANT_BRANCH_STAR("Asymptotic Giant Branch Star", ClassificationType.STAR, "AB*", "AB?"),
    BETA_CEP_VARIABLE("beta Cep Variable", ClassificationType.STAR, "bC*", "bC?"),
    BE_STAR("Be Star", ClassificationType.STAR, "Be*", "Be?"),
    BLACK_HOLE("Black Hole", ClassificationType.GRAVITATION, "BH", "BH?"),
    BLAZAR("Blazar", ClassificationType.GALAXY, "Bla", "Bz?"),
    BLUE_COMPACT_GALAXY("Blue Compact Galaxy", ClassificationType.GALAXY, "bCG"),
    BLUE_OBJECT("Blue Object", ClassificationType.SPECTRAL, "blu"),
    BLUE_STRAGGLER("Blue Straggler", ClassificationType.STAR, "BS*", "BS?"),
    BLUE_SUPERGIANT("Blue Supergiant", ClassificationType.STAR, "s*b", "s?b"),
    BL_LAC("BL Lac", ClassificationType.GALAXY, "BLL", "BL?"),
    BRIGHTEST_GALAXY_IN_A_CLUSTER_BCG("Brightest Galaxy in a Cluster (BCG)", ClassificationType.GALAXY, "BiC"),
    BROWN_DWARF("Brown Dwarf", ClassificationType.STAR, "BD*", "BD?"),
    BUBBLE("Bubble", ClassificationType.INTERSTELLAR_MEDIUM, "bub"),
    BY_DRA_VARIABLE("BY Dra Variable", ClassificationType.STAR, "BY*", "BY?"),
    CARBON_STAR("Carbon Star", ClassificationType.STAR, "C*", "C*?"),
    CATACLYSMIC_BINARY("Cataclysmic Binary", ClassificationType.STAR, "CV*", "CV?"),
    CENTIMETRIC_RADIO_SOURCE("Centimetric Radio Source", ClassificationType.SPECTRAL, "cm"),
    CEPHEID_VARIABLE("Cepheid Variable", ClassificationType.STAR, "Ce*", "Ce?"),
    CHEMICALLY_PECULIAR_STAR("Chemically Peculiar Star", ClassificationType.STAR, "Pe*", "Pe?"),
    CLASSICAL_CEPHEID_VARIABLE("Classical Cepheid Variable", ClassificationType.STAR, "cC*"),
    CLASSICAL_NOVA("Classical Nova", ClassificationType.STAR, "No*", "No?"),
    CLOUD("Cloud", ClassificationType.INTERSTELLAR_MEDIUM, "Cld"),
    CLUSTER_OF_GALAXIES("Cluster of Galaxies", ClassificationType.SET_OF_GALAXIES, "ClG", "C?G"),
    CLUSTER_OF_STARS("Cluster of Stars", ClassificationType.SET_OF_STARS, "Cl*", "Cl?", "C?*"),
    COMETARY_GLOBULE_PILLAR("Cometary Globule / Pillar", ClassificationType.INTERSTELLAR_MEDIUM, "CGb"),
    COMPACT_GROUP_OF_GALAXIES("Compact Group of Galaxies", ClassificationType.SET_OF_GALAXIES, "CGG"),
    COMPOSITE_OBJECT_BLEND("Composite Object, Blend", ClassificationType.OTHER, "mul"),
    DARK_CLOUD_NEBULA("Dark Cloud (nebula)", ClassificationType.INTERSTELLAR_MEDIUM, "DNe"),
    DELTA_SCT_VARIABLE("delta Sct Variable", ClassificationType.STAR, "dS*"),
    DENSE_CORE("Dense Core", ClassificationType.INTERSTELLAR_MEDIUM, "cor"),
    DOUBLE_OR_MULTIPLE_STAR("Double or Multiple Star", ClassificationType.STAR, "**", "**?"),
    ECLIPSING_BINARY("Eclipsing Binary", ClassificationType.STAR, "EB*", "EB?"),
    ELLIPSOIDAL_VARIABLE("Ellipsoidal Variable", ClassificationType.STAR, "El*", "El?"),
    EMISSION_LINE_GALAXY("Emission-line galaxy", ClassificationType.GALAXY, "EmG"),
    EMISSION_LINE_STAR("Emission-line Star", ClassificationType.STAR, "Em*"),
    EMISSION_OBJECT("Emission Object", ClassificationType.SPECTRAL, "EmO"),
    ERUPTIVE_VARIABLE("Eruptive Variable", ClassificationType.STAR, "Er*", "Er?"),
    EVOLVED_STAR("Evolved Star", ClassificationType.STAR, "Ev*", "Ev?"),
    EVOLVED_SUPERGIANT("Evolved Supergiant", ClassificationType.STAR, "sg*", "sg?"),
    EXTRA_SOLAR_PLANET("Extra-solar Planet", ClassificationType.STAR, "Pl", "Pl?"),
    FAR_IR_SOURCE_30_M("Far-IR source (λ >= 30 µm)", ClassificationType.SPECTRAL, "FIR"),
    GALAXY("Galaxy", ClassificationType.GALAXY, "G", "G?"),
    GALAXY_IN_PAIR_OF_GALAXIES("Galaxy in Pair of Galaxies", ClassificationType.GALAXY, "GiP"),
    GALAXY_TOWARDS_A_CLUSTER_OF_GALAXIES("Galaxy towards a Cluster of Galaxies", ClassificationType.GALAXY, "GiC"),
    GALAXY_TOWARDS_A_GROUP_OF_GALAXIES("Galaxy towards a Group of Galaxies", ClassificationType.GALAXY, "GiG"),
    GAMMA_DOR_VARIABLE("gamma Dor Variable", ClassificationType.STAR, "gD*"),
    GAMMA_RAY_BURST("Gamma-ray Burst", ClassificationType.SPECTRAL, "gB"),
    GAMMA_RAY_SOURCE("Gamma-ray Source", ClassificationType.SPECTRAL, "gam"),
    GLOBULAR_CLUSTER("Globular Cluster", ClassificationType.SET_OF_STARS, "GlC", "Gl?"),
    GLOBULE_LOW_MASS_DARK_CLOUD("Globule (low-mass dark cloud)", ClassificationType.INTERSTELLAR_MEDIUM, "glb"),
    GRAVITATIONALLY_LENSED_IMAGE("Gravitationally Lensed Image", ClassificationType.GRAVITATION, "LeI", "LI?"),
    GRAVITATIONALLY_LENSED_IMAGE_OF_A_GALAXY("Gravitationally Lensed Image of a Galaxy", ClassificationType.GRAVITATION, "LeG"),
    GRAVITATIONALLY_LENSED_IMAGE_OF_A_QUASAR("Gravitationally Lensed Image of a Quasar", ClassificationType.GRAVITATION, "LeQ"),
    GRAVITATIONAL_LENS("Gravitational Lens", ClassificationType.GRAVITATION, "gLe", "Le?"),
    GRAVITATIONAL_LENS_SYSTEM_LENS_IMAGES("Gravitational Lens System (lens+images)", ClassificationType.GRAVITATION, "gLS", "LS?"),
    GRAVITATIONAL_SOURCE("Gravitational Source", ClassificationType.GRAVITATION, "grv"),
    GRAVITATIONAL_WAVE_EVENT("Gravitational Wave Event", ClassificationType.GRAVITATION, "GWE"),
    GROUP_OF_GALAXIES("Group of Galaxies", ClassificationType.SET_OF_GALAXIES, "GrG", "Gr?"),
    HERBIG_AE_BE_STAR("Herbig Ae/Be Star", ClassificationType.STAR, "Ae*", "Ae?"),
    HERBIG_HARO_OBJECT("Herbig-Haro Object", ClassificationType.STAR, "HH"),
    HIGH_MASS_X_RAY_BINARY("High Mass X-ray Binary", ClassificationType.STAR, "HXB", "HX?"),
    HIGH_PROPER_MOTION_STAR("High Proper Motion Star", ClassificationType.STAR, "PM*"),
    HIGH_VELOCITY_CLOUD("High-velocity Cloud", ClassificationType.INTERSTELLAR_MEDIUM, "HVC"),
    HIGH_VELOCITY_STAR("High Velocity Star", ClassificationType.STAR, "HV*"),
    HII_GALAXY("HII Galaxy", ClassificationType.GALAXY, "H2G"),
    HII_REGION("HII Region", ClassificationType.INTERSTELLAR_MEDIUM, "HII"),
    HI_21CM_SOURCE("HI (21cm) Source", ClassificationType.SPECTRAL, "HI"),
    HORIZONTAL_BRANCH_STAR("Horizontal Branch Star", ClassificationType.STAR, "HB*", "HB?"),
    HOT_SUBDWARF("Hot Subdwarf", ClassificationType.STAR, "HS*", "HS?"),
    INFRA_RED_SOURCE("Infra-Red Source", ClassificationType.SPECTRAL, "IR"),
    INTERACTING_GALAXIES("Interacting Galaxies", ClassificationType.SET_OF_GALAXIES, "IG"),
    INTERSTELLAR_FILAMENT("Interstellar Filament", ClassificationType.INTERSTELLAR_MEDIUM, "flt"),
    INTERSTELLAR_MEDIUM_OBJECT("Interstellar Medium Object", ClassificationType.INTERSTELLAR_MEDIUM, "ISM"),
    INTERSTELLAR_SHELL("Interstellar Shell", ClassificationType.INTERSTELLAR_MEDIUM, "sh"),
    IRREGULAR_VARIABLE("Irregular Variable", ClassificationType.STAR, "Ir*"),
    LINER_TYPE_ACTIVE_GALAXY_NUCLEUS("LINER-type Active Galaxy Nucleus", ClassificationType.GALAXY, "LIN"),
    LONG_PERIOD_VARIABLE("Long-Period Variable", ClassificationType.STAR, "LP*", "LP?"),
    LOW_MASS_STAR("Low-mass Star", ClassificationType.STAR, "LM*", "LM?"),
    LOW_MASS_X_RAY_BINARY("Low Mass X-ray Binary", ClassificationType.STAR, "LXB", "LX?"),
    LOW_SURFACE_BRIGHTNESS_GALAXY("Low Surface Brightness Galaxy", ClassificationType.GALAXY, "LSB"),
    MAIN_SEQUENCE_STAR("Main Sequence Star", ClassificationType.STAR, "MS*", "MS?"),
    MASER("Maser", ClassificationType.SPECTRAL, "Mas"),
    MASSIVE_STAR("Massive Star", ClassificationType.STAR, "Ma*", "Ma?"),
    METRIC_RADIO_SOURCE("Metric Radio Source", ClassificationType.SPECTRAL, "mR"),
    MICRO_LENSING_EVENT("(Micro)Lensing Event", ClassificationType.GRAVITATION, "Lev"),
    MID_IR_SOURCE_3_TO_30_M("Mid-IR Source (3 to 30 µm)", ClassificationType.SPECTRAL, "MIR"),
    MILLIMETRIC_RADIO_SOURCE("Millimetric Radio Source", ClassificationType.SPECTRAL, "mm"),
    MIRA_VARIABLE("Mira Variable", ClassificationType.STAR, "Mi*", "Mi?"),
    MOLECULAR_CLOUD("Molecular Cloud", ClassificationType.INTERSTELLAR_MEDIUM, "MoC"),
    MOVING_GROUP("Moving Group", ClassificationType.SET_OF_STARS, "MGr"),
    NEAR_IR_SOURCE_3_M("Near-IR Source (λ < 3 µm)", ClassificationType.SPECTRAL, "NIR"),
    NEBULA("Nebula", ClassificationType.INTERSTELLAR_MEDIUM, "GNe"),
    NEUTRON_STAR("Neutron Star", ClassificationType.STAR, "N*", "N*?"),
    NOT_AN_OBJECT_ERROR_ARTEFACT("Not an Object (Error, Artefact, ...)", ClassificationType.OTHER, "err"),
    OBJECT_OF_UNKNOWN_NATURE("Object of Unknown Nature", ClassificationType.OTHER, "?"),
    OH_IR_STAR("OH/IR Star", ClassificationType.STAR, "OH*", "OH?"),
    OPEN_CLUSTER("Open Cluster", ClassificationType.SET_OF_STARS, "OpC"),
    OPTICAL_SOURCE("Optical Source", ClassificationType.SPECTRAL, "Opt"),
    ORION_VARIABLE("Orion Variable", ClassificationType.STAR, "Or*"),
    OUTFLOW("Outflow", ClassificationType.STAR, "out", "of?"),
    PAIR_OF_GALAXIES("Pair of Galaxies", ClassificationType.SET_OF_GALAXIES, "PaG"),
    PART_OF_A_GALAXY("Part of a Galaxy", ClassificationType.OTHER, "PoG"),
    PART_OF_CLOUD("Part of Cloud", ClassificationType.OTHER, "PoC"),
    PLANETARY_NEBULA("Planetary Nebula", ClassificationType.STAR, "PN", "PN?"),
    POST_AGB_STAR("Post-AGB Star", ClassificationType.STAR, "pA*", "pA?"),
    PROTO_CLUSTER_OF_GALAXIES("Proto Cluster of Galaxies", ClassificationType.SET_OF_GALAXIES, "PCG"),
    PULSAR("Pulsar", ClassificationType.STAR, "Psr"),
    PULSATING_VARIABLE("Pulsating Variable", ClassificationType.STAR, "Pu*", "Pu?"),
    QUASAR("Quasar", ClassificationType.GALAXY, "QSO", "Q?"),
    RADIO_BURST("Radio Burst", ClassificationType.SPECTRAL, "rB"),
    RADIO_GALAXY("Radio Galaxy", ClassificationType.GALAXY, "rG"),
    RADIO_SOURCE("Radio Source", ClassificationType.SPECTRAL, "Rad"),
    RED_GIANT_BRANCH_STAR("Red Giant Branch star", ClassificationType.STAR, "RG*", "RB?"),
    RED_SUPERGIANT("Red Supergiant", ClassificationType.STAR, "s*r", "s?r"),
    REFLECTION_NEBULA("Reflection Nebula", ClassificationType.INTERSTELLAR_MEDIUM, "RNe"),
    REGION_DEFINED_IN_THE_SKY("Region defined in the Sky", ClassificationType.OTHER, "reg"),
    ROTATING_VARIABLE("Rotating Variable", ClassificationType.STAR, "Ro*", "Ro?"),
    RR_LYRAE_VARIABLE("RR Lyrae Variable", ClassificationType.STAR, "RR*", "RR?"),
    RS_CVN_VARIABLE("RS CVn Variable", ClassificationType.STAR, "RS*", "RS?"),
    RV_TAURI_VARIABLE("RV Tauri Variable", ClassificationType.STAR, "RV*", "RV?"),
    R_CRB_VARIABLE("R CrB Variable", ClassificationType.STAR, "RC*", "RC?"),
    SEYFERT_1_GALAXY("Seyfert 1 Galaxy", ClassificationType.GALAXY, "Sy1"),
    SEYFERT_2_GALAXY("Seyfert 2 Galaxy", ClassificationType.GALAXY, "Sy2"),
    SEYFERT_GALAXY("Seyfert Galaxy", ClassificationType.GALAXY, "SyG"),
    SPECTROSCOPIC_BINARY("Spectroscopic Binary", ClassificationType.STAR, "SB*", "SB?"),
    STAR("Star", ClassificationType.STAR, "*"),
    STARBURST_GALAXY("Starburst Galaxy", ClassificationType.GALAXY, "SBG"),
    STAR_FORMING_REGION("Star Forming Region", ClassificationType.INTERSTELLAR_MEDIUM, "SFR"),
    STELLAR_STREAM("Stellar Stream", ClassificationType.SET_OF_STARS, "St*"),
    SUB_MILLIMETRIC_SOURCE("Sub-Millimetric Source", ClassificationType.SPECTRAL, "smm"),
    SUPERCLUSTER_OF_GALAXIES("Supercluster of Galaxies", ClassificationType.SET_OF_GALAXIES, "SCG", "SC?"),
    SUPERNOVA("SuperNova", ClassificationType.STAR, "SN*", "SN?"),
    SUPERNOVA_REMNANT("SuperNova Remnant", ClassificationType.INTERSTELLAR_MEDIUM, "SNR", "SR?"),
    SX_PHE_VARIABLE("SX Phe Variable", ClassificationType.STAR, "SX*"),
    SYMBIOTIC_STAR("Symbiotic Star", ClassificationType.STAR, "Sy*", "Sy?"),
    S_STAR("S Star", ClassificationType.STAR, "S*", "S*?"),
    TRANSIENT_EVENT("Transient Event", ClassificationType.SPECTRAL, "ev"),
    TYPE_II_CEPHEID_VARIABLE("Type II Cepheid Variable", ClassificationType.STAR, "WV*", "WV?"),
    T_TAURI_STAR("T Tauri Star", ClassificationType.STAR, "TT*", "TT?"),
    ULTRA_LUMINOUS_X_RAY_SOURCE("Ultra-luminous X-ray Source", ClassificationType.SPECTRAL, "ULX", "UX?"),
    UNDERDENSE_REGION_OF_THE_UNIVERSE("Underdense Region of the Universe", ClassificationType.SET_OF_GALAXIES, "vid"),
    UV_EMISSION_SOURCE("UV-emission Source", ClassificationType.SPECTRAL, "UV"),
    VARIABLE_STAR("Variable Star", ClassificationType.STAR, "V*", "V*?"),
    VARIABLE_SOURCE("Variable Source", ClassificationType.SPECTRAL, "var"),
    WHITE_DWARF("White Dwarf", ClassificationType.STAR, "WD*", "WD?"),
    WOLF_RAYET("Wolf-Rayet", ClassificationType.STAR, "WR*", "WR?"),
    X_RAY_BINARY("X-ray Binary", ClassificationType.STAR, "XB*", "XB?"),
    X_RAY_SOURCE("X-ray Source", ClassificationType.SPECTRAL, "X"),
    YELLOW_SUPERGIANT("Yellow Supergiant", ClassificationType.STAR, "s*y", "s?y"),
    YOUNG_STELLAR_OBJECT("Young Stellar Object", ClassificationType.STAR, "Y*O", "Y*?");

    companion object {

        private val MAPPED = HashMap<String, SkyObjectType>(entries.size * 2)
            .apply { SkyObjectType.entries.forEach { entry -> entry.codes.forEach { this[it] = entry } } }

        @JvmStatic
        fun parse(type: String) = MAPPED[type]
    }
}
