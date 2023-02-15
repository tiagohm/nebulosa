package nebulosa.query.simbad

import nebulosa.math.Angle

class SimbadQuery {

    data class Vertice(val ra: Angle, val dec: Angle)

    private val data = HashMap<String, Any>(8)

    fun limit(value: Int) = apply { data["LIMIT"] = value }

    fun name(value: String) = apply { data["NAME"] = value }

    fun catalog(value: CatalogType) = name("${value.prefix}%")

    fun id(value: Int) = apply { data["ID"] = value }

    fun magnitude(value: ClosedFloatingPointRange<Double>) = apply { data["MAGNITUDE"] = value }

    fun magnitude(min: Double = -100.0, max: Double = 100.0) = magnitude(min..max)

    fun circle(
        centerRA: Angle, centerDEC: Angle, radius: Angle,
    ) = apply { data["CIRCLE"] = doubleArrayOf(centerRA.degrees, centerDEC.degrees, radius.degrees) }

    fun box(
        centerRA: Angle, centerDEC: Angle,
        width: Angle, height: Angle,
    ) = apply { data["BOX"] = doubleArrayOf(centerRA.degrees, centerDEC.degrees, width.degrees, height.degrees) }

    fun polygon(vararg vertices: Vertice) = apply { data["POLYGON"] = vertices.toList() }

    @Suppress("UNCHECKED_CAST")
    internal fun build(): String {
        val limit = data["LIMIT"] as? Int ?: 0
        val name = data["NAME"] as? String ?: ""
        val id = data["ID"] as? Int ?: 0
        val magnitude = data["MAGNITUDE"] as? ClosedFloatingPointRange<Double>

        val builder = StringBuilder(1024)

        if (limit > 0) builder.appendLine("SELECT TOP $limit")
        else builder.appendLine("SELECT")

        builder.appendLine(
            """
                b.oid, b.main_id, b.ra, b.dec, b.pmra, b.pmdec, b.plx_value as plx,
                b.otype, b.sp_type as sptype, b.morph_type as mtype, b.galdim_majaxis as majaxis,
                b.galdim_minaxis as minaxis, f.U, f.B, f.V, f.R, f.I, f.J, f.H, f.K, b.rvz_redshift, b.rvz_radvel,
                ids.ids
                FROM basic as b
                LEFT JOIN allfluxes f ON b.oid = f.oidref
                LEFT JOIN ids ON b.oid = ids.oidref
            """.trimIndent()
        )

        if (name.isNotBlank()) builder.appendLine("LEFT JOIN ident ON b.oid = ident.oidref")

        builder.appendLine("WHERE b.ra IS NOT NULL")
        builder.appendLine("AND b.dec IS NOT NULL")
        builder.appendLine("AND b.otype IS NOT NULL")

        if (id > 0) builder.appendLine("AND b.oid >= $id")
        if (magnitude != null) builder.appendLine("AND (f.V BETWEEN ${magnitude.start} AND ${magnitude.endInclusive}")
            .appendLine("OR f.B BETWEEN ${magnitude.start} AND ${magnitude.endInclusive})")

        if ("CIRCLE" in data) {
            val (ra, dec, radius) = data["CIRCLE"]!! as DoubleArray
            builder.appendLine("AND CONTAINS(POINT('ICRS', ra, dec), CIRCLE('ICRS', $ra, $dec, $radius)) = 1")
        } else if ("BOX" in data) {
            val (ra, dec, width, height) = data["BOX"]!! as DoubleArray
            builder.appendLine("AND CONTAINS(POINT('ICRS', ra, dec), BOX('ICRS', $ra, $dec, $width, $height)) = 1")
        } else if ("POLYGON" in data) {
            val vertices = (data["POLYGON"]!! as List<Vertice>).joinToString(",") { "${it.ra}, ${it.dec}" }
            builder.appendLine("AND CONTAINS(POINT('ICRS', ra, dec), BOX('ICRS', $vertices)) = 1")
        }

        if (name.isNotBlank()) {
            if ("%" in name) builder.appendLine("AND ident.id LIKE '${name.replace("'", "''")}'")
            else builder.appendLine("AND ident.id = '${name.replace("'", "''")}'")
        }

        builder.append("ORDER BY oid ASC")

        return "$builder"
    }
}
