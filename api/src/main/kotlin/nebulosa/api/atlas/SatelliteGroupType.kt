package nebulosa.api.atlas

enum class SatelliteGroupType(
    val group: String,
    val description: String,
) {
    LAST_30_DAYS("last-30-days", "Last 30 Days' Launches"),
    STATIONS("stations", "Space Stations"),
    VISUAL("visual", "100 (or so) Brightest"),
    ACTIVE("active", "Active Satellites"),
    ANALYST("analyst", "Analyst Satellites"),
    COSMOS_1408_DEBRIS("1982-092", "Russian ASAT Test Debris (COSMOS 1408)"),
    FENGYUN_1C_DEBRIS("1999-025", "Chinese ASAT Test Debris (FENGYUN 1C)"),
    IRIDIUM_33_DEBRIS("iridium-33-debris", "IRIDIUM 33 Debris"),
    COSMOS_2251_DEBRIS("cosmos-2251-debris", "COSMOS 2251 Debris"),
    WEATHER("weather", "Weather"),
    NOAA("noaa", "NOAA"),
    GOES("goes", "GOES"),
    RESOURCE("resource", "Earth Resources"),
    SARSAT("sarsat", "Search &amp; Rescue (SARSAT)"),
    DMC("dmc", "Disaster Monitoring"),
    TDRSS("tdrss", "Tracking and Data Relay Satellite System (TDRSS)"),
    ARGOS("argos", "ARGOS Data Collection System"),
    PLANET("planet", "Planet"),
    SPIRE("spire", "Spire"),
    GEO("geo", "Active Geosynchronous"),
    INTELSAT("intelsat", "Intelsat"),
    SES("ses", "SES"),
    IRIDIUM("iridium", "Iridium"),
    IRIDIUM_NEXT("iridium-NEXT", "Iridium NEXT"),
    STARLINK("starlink", "Starlink"),
    ONEWEB("oneweb", "OneWeb"),
    ORBCOMM("orbcomm", "Orbcomm"),
    GLOBALSTAR("globalstar", "Globalstar"),
    SWARM("swarm", "Swarm"),
    AMATEUR("amateur", "Amateur Radio"),
    X_COMM("x-comm", "Experimental Comm"),
    OTHER_COMM("other-comm", "Other Comm"),
    SATNOGS("satnogs", "SatNOGS"),
    GORIZONT("gorizont", "Gorizont"),
    RADUGA("raduga", "Raduga"),
    MOLNIYA("molniya", "Molniya"),
    GNSS("gnss", "GNSS"),
    GPS_OPS("gps-ops", "GPS Operational"),
    GLO_OPS("glo-ops", "GLONASS Operational"),
    GALILEO("galileo", "Galileo"),
    BEIDOU("beidou", "Beidou"),
    SBAS("sbas", "Satellite-Based Augmentation System (WAAS/EGNOS/MSAS)"),
    NNSS("nnss", "Navy Navigation Satellite System (NNSS)"),
    MUSSON("musson", "Russian LEO Navigation"),
    SCIENCE("science", "Space &amp; Earth Science"),
    GEODETIC("geodetic", "Geodetic"),
    ENGINEERING("engineering", "Engineering"),
    EDUCATION("education", "Education"),
    MILITARY("military", "Miscellaneous Military"),
    RADAR("radar", "Radar Calibration"),
    CUBESAT("cubesat", "CubeSats"),
    OTHER("other", "Other Satellites");

    companion object {

        fun codeOf(entries: List<SatelliteGroupType>): Long {
            return entries.fold(0L) { a, b -> a or (1L shl b.ordinal) }
        }
    }
}
