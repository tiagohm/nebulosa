package nebulosa.nasa.spk

/**
 * SPICE system kernels and routines refer to ephemeris objects, reference frames,
 * and instruments by integer codes, usually referred as the ID.
 *
 * @see <a href="https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/C/req/naif_ids.html">Reference</a>
 */
@Suppress("NOTHING_TO_INLINE")
data object NAIF {

    // Barycenters

    const val SOLAR_SYSTEM_BARYCENTER = 0
    const val SSB = SOLAR_SYSTEM_BARYCENTER
    const val MERCURY_BARYCENTER = 1
    const val VENUS_BARYCENTER = 2
    const val EARTH_BARYCENTER = 3
    const val EARTH_MOON_BARYCENTER = 3
    const val EMB = EARTH_MOON_BARYCENTER
    const val MARS_BARYCENTER = 4
    const val JUPITER_BARYCENTER = 5
    const val SATURN_BARYCENTER = 6
    const val URANUS_BARYCENTER = 7
    const val NEPTUNE_BARYCENTER = 8
    const val PLUTO_BARYCENTER = 9
    const val SUN = 10

    // Planets and Satellites

    const val MERCURY = 199
    const val VENUS = 299
    const val EARTH = 399
    const val MOON = 301
    const val MARS = 499
    const val PHOBOS = 401
    const val DEIMOS = 402
    const val JUPITER = 599
    const val IO = 501
    const val EUROPA = 502
    const val GANYMEDE = 503
    const val CALLISTO = 504
    const val AMALTHEA = 505
    const val HIMALIA = 506
    const val ELARA = 507
    const val PASIPHAE = 508
    const val SINOPE = 509
    const val LYSITHEA = 510
    const val CARME = 511
    const val ANANKE = 512
    const val LEDA = 513
    const val THEBE = 514
    const val ADRASTEA = 515
    const val METIS = 516
    const val CALLIRRHOE = 517
    const val THEMISTO = 518
    const val MEGACLITE = 519
    const val TAYGETE = 520
    const val CHALDENE = 521
    const val HARPALYKE = 522
    const val KALYKE = 523
    const val IOCASTE = 524
    const val ERINOME = 525
    const val ISONOE = 526
    const val PRAXIDIKE = 527
    const val AUTONOE = 528
    const val THYONE = 529
    const val HERMIPPE = 530
    const val AITNE = 531
    const val EURYDOME = 532
    const val EUANTHE = 533
    const val EUPORIE = 534
    const val ORTHOSIE = 535
    const val SPONDE = 536
    const val KALE = 537
    const val PASITHEE = 538
    const val HEGEMONE = 539
    const val MNEME = 540
    const val AOEDE = 541
    const val THELXINOE = 542
    const val ARCHE = 543
    const val KALLICHORE = 544
    const val HELIKE = 545
    const val CARPO = 546
    const val EUKELADE = 547
    const val CYLLENE = 548
    const val KORE = 549
    const val HERSE = 550
    const val DIA = 553
    const val SATURN = 699
    const val MIMAS = 601
    const val ENCELADUS = 602
    const val TETHYS = 603
    const val DIONE = 604
    const val RHEA = 605
    const val TITAN = 606
    const val HYPERION = 607
    const val IAPETUS = 608
    const val PHOEBE = 609
    const val JANUS = 610
    const val EPIMETHEUS = 611
    const val HELENE = 612
    const val TELESTO = 613
    const val CALYPSO = 614
    const val ATLAS = 615
    const val PROMETHEUS = 616
    const val PANDORA = 617
    const val PAN = 618
    const val YMIR = 619
    const val PAALIAQ = 620
    const val TARVOS = 621
    const val IJIRAQ = 622
    const val SUTTUNGR = 623
    const val KIVIUQ = 624
    const val MUNDILFARI = 625
    const val ALBIORIX = 626
    const val SKATHI = 627
    const val ERRIAPUS = 628
    const val SIARNAQ = 629
    const val THRYMR = 630
    const val NARVI = 631
    const val METHONE = 632
    const val PALLENE = 633
    const val POLYDEUCES = 634
    const val DAPHNIS = 635
    const val AEGIR = 636
    const val BEBHIONN = 637
    const val BERGELMIR = 638
    const val BESTLA = 639
    const val FARBAUTI = 640
    const val FENRIR = 641
    const val FORNJOT = 642
    const val HATI = 643
    const val HYRROKKIN = 644
    const val KARI = 645
    const val LOGE = 646
    const val SKOLL = 647
    const val SURTUR = 648
    const val ANTHE = 649
    const val JARNSAXA = 650
    const val GREIP = 651
    const val TARQEQ = 652
    const val AEGAEON = 653

    const val URANUS = 799
    const val ARIEL = 701
    const val UMBRIEL = 702
    const val TITANIA = 703
    const val OBERON = 704
    const val MIRANDA = 705
    const val CORDELIA = 706
    const val OPHELIA = 707
    const val BIANCA = 708
    const val CRESSIDA = 709
    const val DESDEMONA = 710
    const val JULIET = 711
    const val PORTIA = 712
    const val ROSALIND = 713
    const val BELINDA = 714
    const val PUCK = 715
    const val CALIBAN = 716
    const val SYCORAX = 717
    const val PROSPERO = 718
    const val SETEBOS = 719
    const val STEPHANO = 720
    const val TRINCULO = 721
    const val FRANCISCO = 722
    const val MARGARET = 723
    const val FERDINAND = 724
    const val PERDITA = 725
    const val MAB = 726
    const val CUPID = 727
    const val NEPTUNE = 899
    const val TRITON = 801
    const val NEREID = 802
    const val NAIAD = 803
    const val THALASSA = 804
    const val DESPINA = 805
    const val GALATEA = 806
    const val LARISSA = 807
    const val PROTEUS = 808
    const val HALIMEDE = 809
    const val PSAMATHE = 810
    const val SAO = 811
    const val LAOMEDEIA = 812
    const val NESO = 813
    const val PLUTO = 999
    const val CHARON = 901
    const val NIX = 902
    const val HYDRA = 903
    const val KERBEROS = 904
    const val STYX = 905

    // Comets

    const val AREND = 1000001
    const val AREND_RIGAUX = 1000002
    const val ASHBROOK_JACKSON = 1000003
    const val BOETHIN = 1000004
    const val BORRELLY = 1000005
    const val BOWELL_SKIFF = 1000006
    const val BRADFIELD = 1000007
    const val BROOKS_2 = 1000008
    const val BRORSEN_METCALF = 1000009
    const val BUS = 1000010
    const val CHERNYKH = 1000011
    const val CHURYUMOV_GERASIMENKO = 1000012
    const val CIFFREO = 1000013
    const val CLARK = 1000014
    const val COMAS_SOLA = 1000015
    const val CROMMELIN = 1000016
    const val D_ARREST = 1000017
    const val DANIEL = 1000018
    const val DE_VICO_SWIFT = 1000019
    const val DENNING_FUJIKAWA = 1000020
    const val DU_TOIT_1 = 1000021
    const val DU_TOIT_HARTLEY = 1000022
    const val DUTOIT_NEUJMIN_DELPORTE = 1000023
    const val DUBIAGO = 1000024
    const val ENCKE = 1000025
    const val FAYE = 1000026
    const val FINLAY = 1000027
    const val FORBES = 1000028
    const val GEHRELS_1 = 1000029
    const val GEHRELS_2 = 1000030
    const val GEHRELS_3 = 1000031
    const val GIACOBINI_ZINNER = 1000032
    const val GICLAS = 1000033
    const val GRIGG_SKJELLERUP = 1000034
    const val GUNN = 1000035
    const val HALLEY = 1000036
    const val HANEDA_CAMPOS = 1000037
    const val HARRINGTON = 1000038
    const val HARRINGTON_ABELL = 1000039
    const val HARTLEY_1 = 1000040
    const val HARTLEY_2 = 1000041
    const val HARTLEY_IRAS = 1000042
    const val HERSCHEL_RIGOLLET = 1000043
    const val HOLMES = 1000044
    const val HONDA_MRKOS_PAJDUSAKOVA = 1000045
    const val HOWELL = 1000046
    const val IRAS = 1000047
    const val JACKSON_NEUJMIN = 1000048
    const val JOHNSON = 1000049
    const val KEARNS_KWEE = 1000050
    const val KLEMOLA = 1000051
    const val KOHOUTEK = 1000052
    const val KOJIMA = 1000053
    const val KOPFF = 1000054
    const val KOWAL_1 = 1000055
    const val KOWAL_2 = 1000056
    const val KOWAL_MRKOS = 1000057
    const val KOWAL_VAVROVA = 1000058
    const val LONGMORE = 1000059
    const val LOVAS_1 = 1000060
    const val MACHHOLZ = 1000061
    const val MAURY = 1000062
    const val NEUJMIN_1 = 1000063
    const val NEUJMIN_2 = 1000064
    const val NEUJMIN_3 = 1000065
    const val OLBERS = 1000066
    const val PETERS_HARTLEY = 1000067
    const val PONS_BROOKS = 1000068
    const val PONS_WINNECKE = 1000069
    const val REINMUTH_1 = 1000070
    const val REINMUTH_2 = 1000071
    const val RUSSELL_1 = 1000072
    const val RUSSELL_2 = 1000073
    const val RUSSELL_3 = 1000074
    const val RUSSELL_4 = 1000075
    const val SANGUIN = 1000076
    const val SCHAUMASSE = 1000077
    const val SCHUSTER = 1000078
    const val SCHWASSMANN_WACHMANN_1 = 1000079
    const val SCHWASSMANN_WACHMANN_2 = 1000080
    const val SCHWASSMANN_WACHMANN_3 = 1000081
    const val SHAJN_SCHALDACH = 1000082
    const val SHOEMAKER_1 = 1000083
    const val SHOEMAKER_2 = 1000084
    const val SHOEMAKER_3 = 1000085
    const val SINGER_BREWSTER = 1000086
    const val SLAUGHTER_BURNHAM = 1000087
    const val SMIRNOVA_CHERNYKH = 1000088
    const val STEPHAN_OTERMA = 1000089
    const val SWIFT_GEHRELS = 1000090
    const val TAKAMIZAWA = 1000091
    const val TAYLOR = 1000092
    const val TEMPEL_1 = 1000093
    const val TEMPEL_2 = 1000094
    const val TEMPEL_TUTTLE = 1000095
    const val TRITTON = 1000096
    const val TSUCHINSHAN_1 = 1000097
    const val TSUCHINSHAN_2 = 1000098
    const val TUTTLE = 1000099
    const val TUTTLE_GIACOBINI_KRESAK = 1000100
    const val VAISALA_1 = 1000101
    const val VAN_BIESBROECK = 1000102
    const val VAN_HOUTEN = 1000103
    const val WEST_KOHOUTEK_IKEMURA = 1000104
    const val WHIPPLE = 1000105
    const val WILD_1 = 1000106
    const val WILD_2 = 1000107
    const val WILD_3 = 1000108
    const val WIRTANEN = 1000109
    const val WOLF = 1000110
    const val WOLF_HARRINGTON = 1000111
    const val LOVAS_2 = 1000112
    const val URATA_NIIJIMA = 1000113
    const val WISEMAN_SKIFF = 1000114
    const val HELIN = 1000115
    const val MUELLER = 1000116
    const val SHOEMAKER_HOLT_1 = 1000117
    const val HELIN_ROMAN_CROCKETT = 1000118
    const val HARTLEY_3 = 1000119
    const val PARKER_HARTLEY = 1000120
    const val HELIN_ROMAN_ALU_1 = 1000121
    const val WILD_4 = 1000122
    const val MUELLER_2 = 1000123
    const val MUELLER_3 = 1000124
    const val SHOEMAKER_LEVY_1 = 1000125
    const val SHOEMAKER_LEVY_2 = 1000126
    const val HOLT_OLMSTEAD = 1000127
    const val METCALF_BREWINGTON = 1000128
    const val LEVY = 1000129
    const val SHOEMAKER_LEVY_9 = 1000130
    const val HYAKUTAKE = 1000131
    const val HALE_BOPP = 1000132
    const val C_2013_A1 = 1003228
    const val SIDING_SPRING = 1003228

    // Asteroids

    const val CERES = 2000001
    const val PALLAS = 2000002
    const val VESTA = 2000004
    const val PSYCHE = 2000016
    const val LUTETIA = 2000021
    const val EUROPA_ASTEROID = 2000052
    const val KLEOPATRA = 2000216
    const val MATHILDE = 2000253
    const val EROS = 2000433
    const val DAVIDA = 2000511
    const val STEINS = 2002867
    const val WILSON_HARRINGTON = 2004015
    const val TOUTATIS = 2004179
    const val BRAILLE = 2009969
    const val ITOKAWA = 2025143
    const val BENNU = 2101955
    const val RYUGU = 2162173
    const val IDA = 2431010
    const val DACTYL = 2431011
    const val ARROKOTH = 2486958
    const val GASPRA = 9511010
    const val PATROCLUS_BARYCENTER = 20000617
    const val EURYBATES_BARYCENTER = 20003548
    const val LEUCUS = 20011351
    const val POLYMELE = 20015094
    const val ORUS = 20021900
    const val DONALDJOHANSON = 20052246
    const val DIDYMOS_BARYCENTER = 20065803
    const val MENOETIUS = 120000617
    const val QUETA = 120003548
    const val DIMORPHOS = 120065803
    const val PATROCLUS = 920000617
    const val EURYBATES = 920003548
    const val DIDYMOS = 920065803

    @Deprecated("Given the need to accommodate many more asteroids expected to be discovered by surveys", ReplaceWith("extendedPermanentAsteroidNumber"))
    inline fun originalPermanentAsteroidNumber(number: Int): Int {
        return 2000000 + number
    }

    inline fun extendedPermanentAsteroidNumber(number: Int): Int {
        return 20000000 + number
    }

    inline fun extendedPrimaryBodyOfPermanentAsteroidNumber(number: Int): Int {
        return 920000000 + number
    }

    inline fun extendedSatelliteOfPermanentAsteroidNumber(number: Int, satellite: Int): Int {
        return satellite * 100000000 + 20000000 + number
    }

    @Deprecated("Given the need to accommodate many more asteroids expected to be discovered by surveys", ReplaceWith("extendedProvisionalAsteroidNumber"))
    inline fun originalProvisionalAsteroidNumber(number: Int): Int {
        return 3000000 + number
    }

    inline fun extendedProvisionalAsteroidNumber(number: Int): Int {
        return 50000000 + number
    }
}
