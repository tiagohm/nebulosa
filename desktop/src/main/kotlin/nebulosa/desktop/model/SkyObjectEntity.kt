package nebulosa.desktop.model

import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.jetbrains.exposed.sql.Column

sealed interface SkyObjectEntity {

    val id: Column<Int>

    val mB: Column<Double>

    val mV: Column<Double>

    val rightAscension: Column<Double>

    val declination: Column<Double>

    val type: Column<SkyObjectType>

    val redshift: Column<Double>

    val parallax: Column<Double>

    val radialVelocity: Column<Double>

    val distance: Column<Double>

    val pmRA: Column<Double>

    val pmDEC: Column<Double>

    val constellation: Column<Constellation>
}
