package nebulosa.simbad

import nebulosa.adql.*
import nebulosa.math.*
import nebulosa.skycatalog.DeepSkyObject
import nebulosa.skycatalog.SkyCatalog
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.time.UTC
import kotlin.math.max
import kotlin.math.min

open class SimbadSkyCatalog(
    private val service: SimbadService,
    estimatedSize: Int = 500,
) : SkyCatalog<DeepSkyObject>(estimatedSize) {

    fun search(
        rightAscension: Angle, declination: Angle, radius: Angle,
        types: List<SkyObjectType> = emptyList(),
        limit: Int = estimatedSize,
    ) {
        clear()

        val builder = QueryBuilder()

        val join: Table = LeftJoin(BASIC_TABLE, FLUX_TABLE, arrayOf(OID equal FLUX_TABLE.column("oidref")))

        builder.add(Distinct)
        builder.add(Limit(max(1, min(limit, 10000))))
        builder.addAll(arrayOf(OID, MAIN_ID, OTYPE, RA, DEC, PM_RA, PM_DEC, PLX, RAD_VEL, REDSHIFT))
        builder.addAll(arrayOf(MAG_V, MAG_B, MAG_U, MAG_R, MAG_I, MAG_J, MAG_H, MAG_K))
        builder.addAll(arrayOf(MAJOR_AXIS, MINOR_AXIS, ORIENT, SP_TYPE))
        builder.addAll(arrayOf(RA.isNotNull, DEC.isNotNull))
        builder.add(join)
        if (radius > 0.0) builder.add(SkyPoint(RA, DEC) contains Circle(rightAscension, declination, radius))
        if (types.isNotEmpty()) builder.add(Or(types.map { OTYPE equal "${it.codes[0]}.." }))
        builder.add(SortBy(OID))

        search(builder.build())
    }

    protected fun search(query: Query) {
        val currentTime = UTC.now()
        val rows = service.query(query).execute().body() ?: emptyList()

        for (row in rows) {
            val id = row.getField("oid").toLong()
            val type = SkyObjectType.parse(row.getField("otype"))!!
            val rightAscensionJ2000 = row.getField("ra").deg
            val declinationJ2000 = row.getField("dec").deg
            val pmRA = row.getField("pmra").toDoubleOrNull()?.mas ?: 0.0
            val pmDEC = row.getField("pmdec").toDoubleOrNull()?.mas ?: 0.0
            val parallax = row.getField("plx_value").toDoubleOrNull() ?: 0.0
            val radialVelocity = row.getField("rvz_radvel").toDoubleOrNull()?.kms ?: 0.0
            val redshift = row.getField("rvz_redshift").toDoubleOrNull() ?: 0.0
            val majorAxis = row.getField("galdim_majaxis").toDoubleOrNull()?.arcmin ?: 0.0
            val minorAxis = row.getField("galdim_minaxis").toDoubleOrNull()?.arcmin ?: 0.0
            val orientation = row.getField("galdim_angle").toDoubleOrNull()?.deg ?: 0.0
            val spType = row.getField("sp_type") ?: ""
            val name = row.getField("main_id")

            var magnitude = row.getField("V").toDoubleOrNull()
                ?: row.getField("B").toDoubleOrNull()
                ?: row.getField("U").toDoubleOrNull()
                ?: SkyObject.UNKNOWN_MAGNITUDE

            if (magnitude >= SkyObject.UNKNOWN_MAGNITUDE || !magnitude.isFinite()) {
                magnitude = min(magnitude, row.getField("R").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, row.getField("I").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, row.getField("J").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, row.getField("H").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, row.getField("K").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
            }

            val distance = if (parallax > 0.0) (1000.0 * ONE_PARSEC) / parallax else 0.0 // AU
            val constellation = SkyObject.computeConstellation(rightAscensionJ2000, declinationJ2000, currentTime)

            val entity = SimbadEntry(
                id, name, magnitude,
                rightAscensionJ2000, declinationJ2000,
                type, spType, majorAxis, minorAxis, orientation,
                pmRA, pmDEC, parallax.mas, radialVelocity, redshift,
                distance.toLightYears, constellation,
            )

            add(entity)
        }
    }

    companion object {

        @JvmStatic private val BASIC_TABLE = From("basic").alias("b")
        @JvmStatic private val FLUX_TABLE = From("allfluxes").alias("f")
        @JvmStatic private val OID = BASIC_TABLE.column("oid")
        @JvmStatic private val MAIN_ID = BASIC_TABLE.column("main_id")
        @JvmStatic private val OTYPE = BASIC_TABLE.column("otype")
        @JvmStatic private val SP_TYPE = BASIC_TABLE.column("sp_type")
        @JvmStatic private val RA = BASIC_TABLE.column("ra")
        @JvmStatic private val DEC = BASIC_TABLE.column("dec")
        @JvmStatic private val PM_RA = BASIC_TABLE.column("pmra")
        @JvmStatic private val PM_DEC = BASIC_TABLE.column("pmdec")
        @JvmStatic private val PLX = BASIC_TABLE.column("plx_value")
        @JvmStatic private val RAD_VEL = BASIC_TABLE.column("rvz_radvel")
        @JvmStatic private val REDSHIFT = BASIC_TABLE.column("rvz_redshift")
        @JvmStatic private val MAJOR_AXIS = BASIC_TABLE.column("galdim_majaxis")
        @JvmStatic private val MINOR_AXIS = BASIC_TABLE.column("galdim_minaxis")
        @JvmStatic private val ORIENT = BASIC_TABLE.column("galdim_angle")
        @JvmStatic private val MAG_V = FLUX_TABLE.column("V")
        @JvmStatic private val MAG_B = FLUX_TABLE.column("B")
        @JvmStatic private val MAG_U = FLUX_TABLE.column("U")
        @JvmStatic private val MAG_R = FLUX_TABLE.column("R")
        @JvmStatic private val MAG_I = FLUX_TABLE.column("I")
        @JvmStatic private val MAG_J = FLUX_TABLE.column("J")
        @JvmStatic private val MAG_H = FLUX_TABLE.column("H")
        @JvmStatic private val MAG_K = FLUX_TABLE.column("K")
    }
}
