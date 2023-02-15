package nebulosa.query.simbad;

enum class CatalogType(val prefix: String) {
    /**
     * Star names following the Bayer's Greek letter system and Flamsteed numbers.
     */
    STAR("* "),

    /**
     * Common name.
     */
    NAME("NAME "),

    /**
     * Henry Draper: 359083 objects.
     */
    HD("HD "),

    /**
     * Harvard obs.: 9110 objects.
     */
    HR("HR "),

    /**
     * Smithsonian Astrophysical Obs.: 258997 objects.
     */
    SAO("SAO "),

    /**
     * Hipparcos: 118218 objects.
     */
    HIP("HIP "),

    /**
     * Tycho: 1058332 objects.
     */
    TYC("TYC "),

    /**
     * New General Catalog: 7840 objects.
     */
    NGC("NGC "),

    /**
     * Index Catalog: 5386 objects.
     */
    IC("IC "),

    /**
     * Messier: 110 objects.
     */
    M("M "),

    /**
     * Caldwell (Not yet in Simbad).
     */
    C("Caldwell "),

    /**
     * Melotte: 245 objects.
     */
    MEL("Cl Melotte "),

    /**
     * Collinder: 471 objects.
     */
    CR("Cl Collinder "),

    /**
     * Barnard: 370 objects.
     */
    B("Barnard "),

    /**
     * Sharpless: 142+313 objects?
     */
    SH("SH "),

    /**
     * SuperNova Remnant.
     */
    SNR("SNR "),

    /**
     * Lynds, Bright Nebula: 1125 objects.
     */
    LBN("LBN "),

    /**
     * Lynds, Dark Nebula: 2009 objects.
     */
    LDN("LDN "),

    /**
     * Rodgers+Campbell+Whiteoak: 182+3 objects.
     */
    RCW("RCW "),

    /**
     * Lyon-Meudon Extragalactic Database.
     * Extension of PGC (Principal Galaxies Catalog).
     * The first designation was PGC, but PGC and LEDA are now equivalent.
     */
    LEDA("LEDA "),

    /**
     * Uppsala General Catalog: 12940 objects.
     */
    UGC("UGC "),

    /**
     * Cederblad: 215 objects.
     */
    CED("CED "),

    /**
     * Atlas of Peculiar Galaxies: +338 objects.
     * Replaces Arp.
     */
    APG("APG "),

    /**
     * Vorontsov-Vel'yaminov (Atlas and Catalogue of Interacting Galaxies): 2014 objects.
     */
    VV("VV "),

    /**
     * Trumpler: 37 objects.
     */
    TR("Cl Trumpler "),

    /**
     * Stock: 21 objects.
     */
    ST("Cl Stock "),

    /**
     * Ruprecht: 176 objects.
     */
    RU("Cl Ruprecht "),

    /**
     * Perek+Kohoutek (Catalogue of galactic planetary nebulae): 1702 objects.
     */
    PK("PK "),

    /**
     * Planetary Nebula: 5465 objects.
     */
    PNG("PN G"),

    /**
     * Abell+Corwin+Olowin: 4076+1174 objects.
     */
    ACO("ACO "),

    /**
     * Hickson, Compact Group.
     */
    HCG("HCG "),

    /**
     * European Southern Obs.
     */
    ESO("ESO "),

    /**
     * Van Den Bergh+Herbst.
     */
    VDBH("VDBH "),

    /**
     * Dickel+Wendker+Bieritz.
     */
    DWB("DWB "),

    /**
     * Guide Star Catalog.
     */
    GSC("GSC "),

    /**
     * Gaia Data Release 1.
     */
    GAIA_DR1("Gaia DR1 "),

    /**
     * Gaia Data Release 2.
     */
    GAIA_DR2("Gaia DR2 "),

    /**
     * Gaia Data Release 3.
     */
    GAIA_DR3("Gaia DR3 ");
}
