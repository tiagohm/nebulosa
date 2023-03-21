import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.arcsec
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Velocity
import nebulosa.math.Velocity.Companion.kms
import nebulosa.simbad.CatalogType
import nebulosa.simbad.SimbadObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.skycatalog.Star
import nebulosa.vizier.VizierTAPService
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream

object BrightStarsGenerator {

    @JvmStatic
    fun main(args: Array<String>) {
        val query = """
            SELECT hip.HIP, cat.HR, cat.Name, cat.HD, cat.SAO, cat.FK5, cat.RAJ2000, cat.DEJ2000, cat.Vmag, cat.SpType, cat.pmRA, cat.pmDE, cat.Parallax, cat.RadVel
            FROM "V/50/catalog" as cat
            LEFT JOIN "I/239/hip_main" as hip ON hip.HD = cat.HD
        """.trimIndent()

        val vizier = VizierTAPService()
        val records = vizier.query(query).execute().body()!!
        val catalog = ArrayList<Star>(records.size)
        var id = 1

        val usedCatalogs = listOf(CatalogType.HD, CatalogType.HIP, CatalogType.SAO, CatalogType.HR, CatalogType.NAME)

        val namedStars = ObjectMapper()
            .readValue(resource("NAMED_STARS.json"), Array<SimbadObject>::class.java)
            .toMutableList()

        for (record in records) {
            val hr = record.get("HR")
            val name = record.get("Name")
            val hip = record.get("HIP")
            val hd = record.get("HD")
            val sao = record.get("SAO")
            val rightAscension = record.get("RAJ2000").toDoubleOrNull()?.deg ?: continue
            val declination = record.get("DEJ2000").toDouble().deg
            val pmRA = record.get("pmRA").toDouble().arcsec
            val pmDE = record.get("pmDE").toDouble().arcsec
            val mV = record.get("Vmag").toDouble()
            val spType = record.get("SpType")
            val plx = record.get("Parallax").toDoubleOrNull()?.arcsec ?: Angle.ZERO
            val rv = record.get("RadVel").toDoubleOrNull()?.kms ?: Velocity.ZERO

            val namedStar = namedStars.firstOrNull { s ->
                hd.isNotEmpty() && s.names.any { it.type == CatalogType.HD && it.name == hd } ||
                        hip.isNotEmpty() && s.names.any { it.type == CatalogType.HIP && it.name == hip } ||
                        hr.isNotEmpty() && s.names.any { it.type == CatalogType.HR && it.name == hr } ||
                        sao.isNotEmpty() && s.names.any { it.type == CatalogType.SAO && it.name == sao }
            }

            val names = HashSet<String>(8)

            if (namedStar != null) {
                names.addAll(namedStar.names.filter { it.type in usedCatalogs }.map { it.type.format(it.name) })
                namedStars.remove(namedStar)
            }

            if (name.isNotEmpty()) names.add(name)
            names.add("HR $hr")
            if (hd.isNotEmpty()) names.add("HD $hd")
            if (sao.isNotEmpty()) names.add("SAO $sao")
            if (hip.isNotEmpty()) names.add("HIP $hip")

            val star = Star(
                id++,
                names.toList(),
                rightAscension = rightAscension,
                declination = declination,
                pmRA = pmRA,
                pmDEC = pmDE,
                mV = mV,
                spType = spType,
                parallax = plx,
                radialVelocity = rv,
                hip = hip,
                hr = hr,
                hd = hd,
                sao = sao,
                type = namedStar?.type ?: SkyObjectType.STAR,
            )

            catalog.add(star)
        }

        println(namedStars.size)

        for (namedStar in namedStars) {
            val hip = namedStar.names.firstOrNull { it.type == CatalogType.HIP }?.name ?: ""
            val hd = namedStar.names.firstOrNull { it.type == CatalogType.HD }?.name ?: ""
            val hr = namedStar.names.firstOrNull { it.type == CatalogType.HR }?.name ?: ""
            val sao = namedStar.names.firstOrNull { it.type == CatalogType.SAO }?.name ?: ""

            val star = Star(
                namedStar.id.toInt(),
                namedStar.names.filter { it.type in usedCatalogs }.map { it.type.format(it.name) },
                rightAscension = namedStar.ra.deg,
                declination = namedStar.dec.deg,
                pmRA = namedStar.pmRA.mas,
                pmDEC = namedStar.pmDEC.mas,
                mV = namedStar.v,
                spType = namedStar.spType,
                parallax = namedStar.plx.mas,
                radialVelocity = namedStar.rv.kms,
                hip = hip,
                hr = hr,
                hd = hd,
                sao = sao,
                type = namedStar.type,
            )

            catalog.add(star)
        }

        println(catalog.size)

        val fos = FileOutputStream(File("nebulosa-skycatalog-brightstars/src/main/resources/BrightStars.dat"))
        val oos = ObjectOutputStream(fos)
        oos.writeObject(catalog)
        oos.flush()
        oos.close()
    }
}
