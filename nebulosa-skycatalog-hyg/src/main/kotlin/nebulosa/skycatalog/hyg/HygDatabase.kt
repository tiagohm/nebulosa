package nebulosa.skycatalog.hyg

import de.siegmar.fastcsv.reader.NamedCsvReader
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Velocity.Companion.kms
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.ICRF
import nebulosa.skycatalog.SkyCatalog
import nebulosa.skycatalog.SkyObject.Companion.NAME_SEPARATOR
import nebulosa.skycatalog.Star
import java.io.InputStream
import java.io.InputStreamReader

/**
 * HYG star database archive.
 *
 * @see <a href="https://github.com/astronexus/HYG-Database">GitHub</a>
 */
class HygDatabase : SkyCatalog<Star>(118005) {

    fun load(stream: InputStream) {
        clear()

        val reader = CSV_READER.build(InputStreamReader(stream, Charsets.UTF_8))

        val names = ArrayList<String>(7)

        for (record in reader) {
            val id = record.getField("id").toLong()
            if (id == 0L) continue
            val hip = record.getField("hip").takeIf { it.isNotEmpty() }?.toInt() ?: 0
            val hd = record.getField("hd").takeIf { it.isNotEmpty() }?.toInt() ?: 0
            val hr = record.getField("hr").takeIf { it.isNotEmpty() }?.toInt() ?: 0
            val rightAscension = record.getField("ra").toDouble().hours
            val declination = record.getField("dec").toDouble().deg
            // val name = record.getField("proper")
            val name = IAU_STAR_NAMES["$hip"] ?: ""
            // TODO: Distance, Parallax.
            // val distance = record.getField("dist").toDouble()
            val pmRA = record.getField("pmra").toDouble().mas
            val pmDEC = record.getField("pmdec").toDouble().mas
            val radialVelocity = record.getField("rv").toDouble().kms
            val magnitude = record.getField("mag").toDouble()
            val spType = record.getField("spect")
            val bayer = record.getField("bayer")
            val flamsteed = record.getField("flam").toIntOrNull() ?: 0
            val constellation = record.getField("con")
                .takeIf { it.isNotEmpty() }
                ?.uppercase()
                ?.let(Constellation::valueOf)
                ?: computeConstellation(rightAscension, declination)

            names.clear()

            if (name.isNotEmpty()) names.add(name)
            if (bayer.isNotEmpty()) names.add("$bayer ${constellation.iau}")
            if (flamsteed > 0) names.add("$flamsteed ${constellation.iau}")
            if (hip > 0) names.add("HIP $hip")
            if (hd > 0) names.add("HD $hd")
            if (hr > 0) names.add("HR $hr")

            if (names.isEmpty()) continue

            val star = Star(
                id = id,
                name = names.joinToString(NAME_SEPARATOR).trim(),
                magnitude = magnitude,
                rightAscensionJ2000 = rightAscension,
                declinationJ2000 = declination,
                spType = spType,
                radialVelocity = radialVelocity,
                pmRA = pmRA,
                pmDEC = pmDEC,
                constellation = constellation,
            )

            add(star)
        }

        notifyLoadFinished()
    }

    companion object {

        @JvmStatic private val CSV_READER = NamedCsvReader.builder()
            .fieldSeparator(',')
            .quoteCharacter('"')
            .commentCharacter('#')
            .skipComments(true)

        @JvmStatic
        private fun computeConstellation(rightAscension: Angle, declination: Angle): Constellation {
            return Constellation.find(ICRF.equatorial(rightAscension, declination))
        }

        // https://www.iau.org/public/themes/naming_stars
        @JvmStatic private val IAU_STAR_NAMES = mapOf(
            "XO-5" to "Absolutno", "104987" to "Kitalpha",
            "13847" to "Acamar", "72607" to "Kochab",
            "7588" to "Achernar", "12961" to "Koeia",
            "3821" to "Achird", "XO-4" to "Koit",
            "78820" to "Acrab", "80816" to "Kornephoros",
            "60718" to "Acrux", "61359" to "Kraz",
            "44066" to "Acubens", "108917" to "Kurhah",
            "50335" to "Adhafera", "62223" to "La Superba",
            "33579" to "Adhara", "82396" to "Larawag",
            "6411" to "Adhil", "HAT-P-42" to "Lerna",
            "20889" to "Ain", "85696" to "Lesath",
            "92761" to "Ainalrami", "97938" to "Libertas",
            "94481" to "Aladfar", "PSR B1257+12" to "Lich",
            "90004" to "Alasia", "66192" to "Liesma",
            "94141" to "Albaldah", "13061" to "Lilii Borea",
            "102618" to "Albali", "110813" to "Lionrock",
            "95947" to "Albireo", "30860" to "Lucilinburhuc",
            "59199" to "Alchiba", "30905" to "Lusitânia",
            "65477" to "Alcor", "85693" to "Maasym",
            "17702" to "Alcyone", "52521" to "Macondo",
            "21421" to "Aldebaran", "24003" to "Mago",
            "105199" to "Alderamin", "28380" to "Mahasim",
            "108085" to "Aldhanab", "82651" to "Mahsati",
            "83895" to "Aldhibah", "17573" to "Maia",
            "101421" to "Aldulfin", "WASP-39" to "Malmok",
            "106032" to "Alfirk", "80883" to "Marfik",
            "100064" to "Algedi", "113963" to "Markab",
            "1067" to "Algenib", "45941" to "Markeb",
            "50583" to "Algieba", "79043" to "Marsic",
            "14576" to "Algol", "112158" to "Matar",
            "60965" to "Algorab", "HAT-P-21" to "Mazaalai",
            "31681" to "Alhena", "32246" to "Mebsuta",
            "62956" to "Alioth", "59774" to "Megrez",
            "102488" to "Aljanah", "26207" to "Meissa",
            "67301" to "Alkaid", "34088" to "Mekbuda",
            "75411" to "Alkalurops", "42556" to "Meleph",
            "44471" to "Alkaphrah", "28360" to "Menkalinan",
            "115623" to "Alkarab", "14135" to "Menkar",
            "53740" to "Alkes", "68933" to "Menkent",
            "23416" to "Almaaz", "18614" to "Menkib",
            "9640" to "Almach", "53910" to "Merak",
            "109268" to "Alnair", "72487" to "Merga",
            "88635" to "Alnasl", "94114" to "Meridiana",
            "26311" to "Alnilam", "17608" to "Merope",
            "26727" to "Alnitak", "8832" to "Mesarthim",
            "80112" to "Alniyat", "45238" to "Miaplacidus",
            "46390" to "Alphard", "62434" to "Mimosa",
            "76267" to "Alphecca", "42402" to "Minchir",
            "677" to "Alpheratz", "63090" to "Minelauva",
            "7097" to "Alpherg", "25930" to "Mintaka",
            "83608" to "Alrakis", "10826" to "Mira",
            "9487" to "Alrescha", "5447" to "Mirach",
            "86782" to "Alruba", "13268" to "Miram",
            "96100" to "Alsafi", "15863" to "Mirfak",
            "41075" to "Alsciaukat", "30324" to "Mirzam",
            "42913" to "Alsephina", "14668" to "Misam",
            "98036" to "Alshain", "65378" to "Mizar",
            "100310" to "Alshat", "XO-1" to "Moldoveanu",
            "97649" to "Altair", "WASP-79" to "Montuno",
            "94376" to "Altais", "WASP-60" to "Morava",
            "46750" to "Alterf", "HAT-P-23" to "Moriah",
            "35904" to "Aludra", "8796" to "Mothallah",
            "55203" to "Alula Australis", "22491" to "Mouhoun",
            "55219" to "Alula Borealis", "WASP-71" to "Mpingo",
            "92946" to "Alya", "34045" to "Muliphein",
            "32362" to "Alzirr", "67927" to "Muphrid",
            "29550" to "Amadioha", "41704" to "Muscida",
            "WASP-34" to "Amansinaya", "103527" to "Musica",
            "WASP-52" to "Anadolu", "HAT-P-29" to "Muspelheim",
            "110003" to "Ancha", "WASP-6" to "Márohu",
            "13288" to "Angetenar", "72339" to "Mönch",
            "57820" to "Aniara", "44946" to "Nahn",
            "2081" to "Ankaa", "WASP-62" to "Naledi",
            "95771" to "Anser", "39429" to "Naos",
            "80763" to "Antares", "106985" to "Nashira",
            "72845" to "Arcalís", "48235" to "Natasha",
            "69673" to "Arcturus", "73555" to "Nekkar",
            "95294" to "Arkab Posterior", "7607" to "Nembus",
            "95241" to "Arkab Prior", "5054" to "Nenque",
            "25985" to "Arneb", "32916" to "Nervia",
            "93506" to "Ascella", "25606" to "Nihal",
            "42911" to "Asellus Australis", "74961" to "Nikawiy",
            "42806" to "Asellus Borealis", "31895" to "Nosaxa",
            "43109" to "Ashlesha", "92855" to "Nunki",
            "45556" to "Aspidiske", "75695" to "Nusakan",
            "17579" to "Asterope", "13192" to "Nushagak",
            "WASP-64" to "Atakoraka", "WASP-15" to "Nyamien",
            "80331" to "Athebyne", "40687" to "Násti",
            "17448" to "Atik", "80838" to "Ogma",
            "17847" to "Atlas", "93747" to "Okab",
            "82273" to "Atria", "81266" to "Paikauhale",
            "41037" to "Avior", "WASP-32" to "Parumleo",
            "118319" to "Axólotl", "100751" to "Peacock",
            "13993" to "Ayeyarwady", "WASP-80" to "Petra",
            "107136" to "Azelfafage", "26634" to "Phact",
            "13701" to "Azha", "58001" to "Phecda",
            "38170" to "Azmidi", "75097" to "Pherkad",
            "73136" to "Baekdu", "99711" to "Phoenicia",
            "87937" to "Barnard's Star", "40881" to "Piautos",
            "8645" to "Baten Kaitos", "88414" to "Pincoya",
            "20535" to "Beemim", "82545" to "Pipirima",
            "19587" to "Beid", "TrES-3" to "Pipoltr",
            "95124" to "Belel", "17851" to "Pleione",
            "25336" to "Bellatrix", "116084" to "Poerava",
            "HAT-P-15" to "Berehynia", "11767" to "Polaris",
            "27989" to "Betelgeuse", "104382" to "Polaris Australis",
            "13209" to "Bharani", "89341" to "Polis",
            "48711" to "Bibhā", "37826" to "Pollux",
            "109427" to "Biham", "61941" to "Porrima",
            "107251" to "Bosona", "53229" to "Praecipua",
            "14838" to "Botein", "20205" to "Prima Hyadum",
            "73714" to "Brachium", "37279" to "Procyon",
            "26380" to "Bubup", "29655" to "Propus",
            "12191" to "Buna", "GJ 551" to "Proxima Centauri",
            "106786" to "Bunda", "16537" to "Ran",
            "6643" to "Bélénos", "17378" to "Rana",
            "30438" to "Canopus", "83547" to "Rapeto",
            "24608" to "Capella", "48455" to "Rasalas",
            "746" to "Caph", "84345" to "Rasalgethi",
            "36850" to "Castor", "86032" to "Rasalhague",
            "4422" to "Castula", "85670" to "Rastaban",
            "86742" to "Cebalrai", "49669" to "Regulus",
            "37284" to "Ceibo", "5737" to "Revati",
            "17489" to "Celaeno", "24436" to "Rigel",
            "86796" to "Cervantes", "71683" to "Rigil Kentaurus",
            "53721" to "Chalawan", "81022" to "Rosaliadecastro",
            "20894" to "Chamukuy", "101769" to "Rotanev",
            "WASP-50" to "Chaophraya", "6686" to "Ruchbah",
            "61317" to "Chara", "95347" to "Rukbat",
            "HAT-P-5" to "Chasoň", "84012" to "Sabik",
            "99894" to "Chechia", "23453" to "Saclateni",
            "54879" to "Chertan", "110395" to "Sadachbia",
            "1547" to "Citadelle", "112748" to "Sadalbari",
            "33719" to "Citalá", "109074" to "Sadalmelik",
            "3479" to "Cocibolca", "106278" to "Sadalsuud",
            "43587" to "Copernicus", "100453" to "Sadr",
            "63125" to "Cor Caroli", "56572" to "Sagarmatha",
            "80463" to "Cujam", "27366" to "Saiph",
            "23875" to "Cursa", "115250" to "Salm",
            "100345" to "Dabih", "HAT-P-34" to "Sansuna",
            "14879" to "Dalim", "86228" to "Sargas",
            "102098" to "Deneb", "84379" to "Sarin",
            "107556" to "Deneb Algedi", "21594" to "Sceptrum",
            "57632" to "Denebola", "113881" to "Scheat",
            "64241" to "Diadem", "3179" to "Schedar",
            "54158" to "Dingolay", "20455" to "Secunda Hyadum",
            "3419" to "Diphda", "8886" to "Segin",
            "WASP-72" to "Diya", "71075" to "Seginus",
            "66047" to "Dofida", "96757" to "Sham",
            "HAT-P-3" to "Dombay", "55664" to "Shama",
            "78401" to "Dschubba", "79431" to "Sharjah",
            "54061" to "Dubhe", "85927" to "Shaula",
            "86614" to "Dziban", "92420" to "Sheliak",
            "WASP-17" to "Dìwö", "8903" to "Sheratan",
            "114322" to "Ebla", "95262" to "Sika",
            "75458" to "Edasich", "32349" to "Sirius",
            "17499" to "Electra", "111710" to "Situla",
            "70755" to "Elgafar", "113136" to "Skat",
            "29034" to "Elkurud", "BD+14-4559" to "Solaris",
            "25428" to "Elnath", "65474" to "Spica",
            "87833" to "Eltanin", "HAT-P-6" to "Sterrennacht",
            "5529" to "Emiw", "43674" to "Stribor",
            "107315" to "Enif", "101958" to "Sualocin",
            "116727" to "Errai", "47508" to "Subra",
            "90344" to "Fafnir", "44816" to "Suhail",
            "78265" to "Fang", "93194" to "Sulafat",
            "97165" to "Fawaris", "69701" to "Syrma",
            "48615" to "Felis", "106824" to "Sāmaya",
            "2247" to "Felixvarela", "22449" to "Tabit",
            "57370" to "Flegetonte", "HAT-P-40" to "Taika",
            "113368" to "Fomalhaut", "57399" to "Taiyangshou",
            "56508" to "Formosa", "63076" to "Taiyi",
            "HAT-P-14" to "Franz", "44127" to "Talitha",
            "2920" to "Fulu", "WASP-21" to "Tangra",
            "113889" to "Fumalsamakah", "50801" to "Tania Australis",
            "61177" to "Funi", "50372" to "Tania Borealis",
            "30122" to "Furud", "38041" to "Tapecue",
            "87261" to "Fuyue", "97278" to "Tarazed",
            "61084" to "Gacrux", "40526" to "Tarf",
            "42446" to "Gakyid", "17531" to "Taygeta",
            "PSR B0633+17" to "Geminga", "40167" to "Tegmine",
            "56211" to "Giausar", "30343" to "Tejat",
            "59803" to "Gienah", "98066" to "Terebellum",
            "60260" to "Ginan", "HAT-P-9" to "Tevel",
            "WASP-13" to "Gloas", "21393" to "Theemin",
            "36188" to "Gomeisa", "68756" to "Thuban",
            "87585" to "Grumium", "112122" to "Tiaki",
            "77450" to "Gudja", "26451" to "Tianguan",
            "94645" to "Gumala", "62423" to "Tianyi",
            "84405" to "Guniibuu", "80687" to "Timir",
            "68702" to "Hadar", "WASP-161" to "Tislit",
            "23767" to "Haedus", "7513" to "Titawin",
            "9884" to "Hamal", "WASP-22" to "Tojil",
            "23015" to "Hassaleh", "71681" to "Toliman",
            "26241" to "Hatysa", "58952" to "Tonatiuh",
            "113357" to "Helvetios", "8198" to "Torcular",
            "66249" to "Heze", "HAT-P-36" to "Tuiren",
            "21109" to "Hoggar", "17096" to "Tupi",
            "112029" to "Homam", "60644" to "Tupã",
            "HAT-P-38" to "Horna", "39757" to "Tureis",
            "55174" to "Hunahpú", "47431" to "Ukdah",
            "80076" to "Hunor", "57291" to "Uklun",
            "78104" to "Iklil", "77070" to "Unukalhai",
            "47087" to "Illyrian", "33856" to "Unurgunite",
            "59747" to "Imai", "96078" to "Uruk",
            "84787" to "Inquill", "91262" to "Vega",
            "15578" to "Intan", "116076" to "Veritate",
            "46471" to "Intercrus", "63608" to "Vindemiatrix",
            "WASP-38" to "Irena", "35550" to "Wasat",
            "108375" to "Itonda", "27628" to "Wazn",
            "72105" to "Izar", "34444" to "Wezen",
            "79374" to "Jabbah", "5348" to "Wurren",
            "37265" to "Jishui", "82514" to "Xamidimura",
            "12706" to "Kaffaljidhma", "91852" to "Xihe",
            "47202" to "Kalausi", "69732" to "Xuange",
            "79219" to "Kamuy", "79882" to "Yed Posterior",
            "69427" to "Kang", "79593" to "Yed Prior",
            "76351" to "Karaka", "85822" to "Yildun",
            "90185" to "Kaus Australis", "60129" to "Zaniah",
            "90496" to "Kaus Borealis", "18543" to "Zaurak",
            "89931" to "Kaus Media", "57757" to "Zavijava",
            "92895" to "Kaveh", "48356" to "Zhang",
            "19849" to "Keid", "15197" to "Zibal",
            "69974" to "Khambalia", "54872" to "Zosma",
            "Zubenelgenubi" to "72622", "Zubenelhakrabi" to "76333",
            "Zubeneschamali" to "74785",
        )
    }
}
