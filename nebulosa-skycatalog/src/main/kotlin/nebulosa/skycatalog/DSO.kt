package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation

data class DSO(
    override val id: Int = 0,
    override val names: String = "",
    val m: Int = 0, // Messier Catalog number
    val ngc: Int = 0, // New General Catalog number
    val ic: Int = 0, // Index Catalog number
    val c: Int = 0, // Caldwell Catalog number
    val b: Int = 0, // Barnard Catalog number (Dark Nebulae)
    val sh2: Int = 0, // Sharpless Catalog number (Catalogue of HII Regions (Sharpless, 1959))
    val vdb: Int = 0, // van den Bergh Catalog number (Catalogue of Reflection Nebulae (van den Bergh, 1966))
    val rcw: Int = 0, // RCW Catalog number (H-α emission regions in Southern Milky Way (Rodgers+, 1960))
    val ldn: Int = 0, // LDN Catalog number (Lynds' Catalogue of Dark Nebulae (Lynds, 1962))
    val lbn: Int = 0, // LBN Catalog number (Lynds' Catalogue of Bright Nebulae (Lynds, 1965))
    val cr: Int = 0, // Collinder Catalog number
    val mel: Int = 0, // Melotte Catalog number
    val pgc: Int = 0, // PGC number (Catalog of galaxies)
    val ugc: Int = 0, // UGC number (The Uppsala General Catalogue of Galaxies)
    val arp: Int = 0, // Arp number (Atlas of Peculiar Galaxies (Arp, 1966))
    val vv: Int = 0, // VV number (The Catalogue of Interacting Galaxies (Vorontsov-Velyaminov+, 2001))
    val dwb: Int = 0, // DWB number (Catalogue and distances of optically visible H II regions (Dickel+, 1969))
    val tr: Int = 0, // Tr number (Trumpler Catalogue)
    val st: Int = 0, // St number (Stock Catalogue)
    val ru: Int = 0, // Ru number (Ruprecht Catalogue)
    val vdbha: Int = 0, // vdB-Ha number (van den Bergh-Hagen Catalogue)
    val ced: String? = null, // Ced number (Cederblad Catalog of bright diffuse Galactic nebulae)
    val pk: String? = null, // PK number (Catalogue of Galactic Planetary Nebulae)
    val png: String? = null, // PN G number (Strasbourg-ESO Catalogue of Galactic Planetary Nebulae (Acker+, 1992))
    val snrg: String? = null, // SNR G number (A catalogue of Galactic supernova remnants (Green, 2014))
    val aco: String? = null, // ACO number (Rich Clusters of Galaxies (Abell+, 1989))
    val hcg: String? = null, // HCG number (Hickson Compact Group (Hickson, 1989))
    val eso: String? = null, // ESO number (ESO/Uppsala Survey of the ESO(B) Atlas (Lauberts, 1982))
    val vdbh: String? = null, // VdBH number (Southern Stars embedded in nebulosity (van den Bergh+, 1975))
    override val magnitude: Double = Double.MAX_VALUE,
    override val rightAscension: Angle = Angle.ZERO,
    override val declination: Angle = Angle.ZERO,
    override val type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    val mType: String? = null,
    override val majorAxis: Angle = Angle.ZERO,
    override val minorAxis: Angle = Angle.ZERO,
    override val orientation: Angle = Angle.ZERO,
    override val redshift: Double = 0.0,
    override val parallax: Angle = Angle.ZERO,
    override val radialVelocity: Velocity = Velocity.ZERO,
    override val distance: Double = 0.0, // Distance (Mpc for galaxies, kpc for other objects)
    override val pmRA: Angle = Angle.ZERO,
    override val pmDEC: Angle = Angle.ZERO,
    override val constellation: Constellation = Constellation.AND,
) : SkyObject, HasAxisSize {

    // Dunlop Catalogue.
    val dunlop
        get() = ngc > 0 && ngc in DUNLOP

    // Bennett Catalogue.
    val bennett
        get() = ngc > 0 && ngc in BENNET || mel == 105 || ic == 1459 || tr == 23

    // Herschel 400 Catalogue.
    val h400
        get() = ngc > 0 && ngc in H400

    companion object {

        @JvmStatic private val serialVersionUID = 2L

        @JvmStatic private val H400 = intArrayOf(
            40, 129, 136, 157, 185, 205, 225, 246, 247, 253, 278, 288,
            381, 404, 436, 457, 488, 524, 559, 584, 596, 598, 613, 615,
            637, 650, 654, 659, 663, 720, 752, 772, 779, 869, 884, 891,
            908, 936, 1022, 1023, 1027, 1052, 1055, 1084, 1245, 1342, 1407, 1444,
            1501, 1502, 1513, 1528, 1535, 1545, 1647, 1664, 1788, 1817, 1857, 1907,
            1931, 1961, 1964, 1980, 1999, 2022, 2024, 2126, 2129, 2158, 2169, 2185,
            2186, 2194, 2204, 2215, 2232, 2244, 2251, 2264, 2266, 2281, 2286, 2301,
            2304, 2311, 2324, 2335, 2343, 2353, 2354, 2355, 2360, 2362, 2371, 2372,
            2392, 2395, 2403, 2419, 2420, 2421, 2422, 2423, 2438, 2440, 2479, 2482,
            2489, 2506, 2509, 2527, 2539, 2548, 2567, 2571, 2613, 2627, 2655, 2681,
            2683, 2742, 2768, 2775, 2782, 2787, 2811, 2841, 2859, 2903, 2950, 2964,
            2974, 2976, 2985, 3034, 3077, 3079, 3115, 3147, 3166, 3169, 3184, 3190,
            3193, 3198, 3226, 3227, 3242, 3245, 3277, 3294, 3310, 3344, 3377, 3379,
            3384, 3395, 3412, 3414, 3432, 3486, 3489, 3504, 3521, 3556, 3593, 3607,
            3608, 3610, 3613, 3619, 3621, 3626, 3628, 3631, 3640, 3655, 3665, 3675,
            3686, 3726, 3729, 3810, 3813, 3877, 3893, 3898, 3900, 3912, 3938, 3941,
            3945, 3949, 3953, 3962, 3982, 3992, 3998, 4026, 4027, 4030, 4036, 4039,
            4041, 4051, 4085, 4088, 4102, 4111, 4143, 4147, 4150, 4151, 4179, 4203,
            4214, 4216, 4245, 4251, 4258, 4261, 4273, 4274, 4278, 4281, 4293, 4303,
            4314, 4346, 4350, 4361, 4365, 4371, 4394, 4414, 4419, 4429, 4435, 4438,
            4442, 4448, 4449, 4450, 4459, 4473, 4477, 4478, 4485, 4490, 4494, 4526,
            4527, 4535, 4536, 4546, 4548, 4550, 4559, 4565, 4570, 4594, 4596, 4618,
            4631, 4636, 4643, 4654, 4656, 4660, 4665, 4666, 4689, 4697, 4698, 4699,
            4725, 4753, 4754, 4762, 4781, 4800, 4845, 4856, 4866, 4900, 4958, 4995,
            5005, 5033, 5054, 5195, 5248, 5273, 5322, 5363, 5364, 5466, 5473, 5474,
            5557, 5566, 5576, 5631, 5634, 5676, 5689, 5694, 5746, 5846, 5866, 5897,
            5907, 5982, 6118, 6144, 6171, 6207, 6217, 6229, 6235, 6284, 6287, 6293,
            6304, 6316, 6342, 6355, 6356, 6369, 6401, 6426, 6440, 6445, 6451, 6514,
            6517, 6520, 6522, 6528, 6540, 6543, 6544, 6553, 6568, 6569, 6583, 6624,
            6629, 6633, 6638, 6642, 6645, 6664, 6712, 6755, 6756, 6781, 6802, 6818,
            6823, 6826, 6830, 6834, 6866, 6882, 6885, 6905, 6910, 6934, 6939, 6940,
            6946, 7000, 7006, 7008, 7009, 7044, 7062, 7086, 7128, 7142, 7160, 7209,
            7217, 7243, 7296, 7331, 7380, 7448, 7479, 7510, 7606, 7662, 7686, 7723,
            7727, 7789, 7790, 7814,
        )

        // Details: http://www.docdb.net/tutorials/bennett_catalogue.php
        // Esp. for southern observers
        // NGC 1291 = NGC 1269
        @JvmStatic private val BENNET = intArrayOf(
            55, 104, 247, 253, 288, 300, 362, 613, 1068, 1097, 1232, 1261,
            1269, 1313, 1316, 1350, 1360, 1365, 1380, 1387, 1399, 1398, 1404, 1433,
            1512, 1535, 1549, 1553, 1566, 1617, 1672, 1763, 1783, 1792, 1818, 1808,
            1851, 1866, 1904, 2070, 2214, 2243, 2298, 2467, 2489, 2506, 2627, 2671,
            2808, 2972, 2997, 3115, 3132, 3201, 3242, 3621, 3960, 3923, 4372, 4590,
            4594, 4697, 4699, 4753, 4833, 4945, 4976, 5061, 5068, 5128, 5139, 5189,
            5236, 5253, 5286, 5617, 5634, 5824, 5897, 5927, 5986, 5999, 6005, 6093,
            6101, 6121, 6134, 6144, 6139, 6171, 6167, 6192, 6218, 6216, 6235, 6254,
            6253, 6266, 6273, 6284, 6287, 6293, 6304, 6316, 6318, 6333, 6356, 6352,
            6362, 6388, 6402, 6397, 6440, 6445, 6441, 6496, 6522, 6528, 6544, 6541,
            6553, 6569, 6584, 6603, 6618, 6624, 6626, 6638, 6637, 6642, 6652, 6656,
            6681, 6705, 6712, 6715, 6723, 6744, 6752, 6809, 6818, 6864, 6981, 7009,
            7089, 7099, 7293, 7410, 7793,
        )

        // Details: http://www.docdb.net/tutorials/dunlop_catalogue.php
        // Esp. for southern observers
        // NGC 1291 = NGC 1269
        @JvmStatic private val DUNLOP = intArrayOf(
            7590, 7599, 104, 330, 346, 362, 6101, 1795, 1943, 2019, 2058, 2122,
            1743, 1910, 1928, 1966, 2070, 2069, 2136, 4833, 1755, 1770, 1936, 2159,
            2164, 2156, 2214, 1313, 1869, 1955, 1974, 2004, 2121, 2035, 6362, 1810,
            1818, 2029, 2027, 1831, 6744, 2808, 4609, 5281, 5316, 3766, 4103, 4349,
            6752, 3114, 4755, 5617, 6025, 3372, 4852, 5606, 3532, 6087, 5715, 6005,
            1261, 5662, 5999, 1515, 3960, 3330, 5749, 5925, 6031, 6067, 6208, 6397,
            6584, 3228, 5286, 5927, 2972, 6167, 7049, 2547, 4945, 6134, 6193, 6352,
            6861, 1433, 5460, 1493, 5139, 6204, 3201, 6216, 6259, 6388, 1512, 5643,
            6541, 625, 1487, 3680, 5128, 6192, 1269, 6231, 55, 1851, 4709, 6124,
            7410, 6242, 6268, 6318, 2477, 6139, 1317, 1316, 1808, 5986, 6281, 6441,
            1436, 2546, 2818, 6400, 6723, 1380, 2298, 1350, 2090, 1532, 6652, 2658,
            6416, 6637, 6681, 3621, 6569, 6809, 5253, 6715, 2489, 6266, 5236,
        )
    }
}
