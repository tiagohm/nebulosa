package nebulosa.constants

const val GM_SUN_PITJEVA_2005_KM3_S2 = 132712440042.0
const val GM_SUN_PITJEVA_2005 = GM_SUN_PITJEVA_2005_KM3_S2 * DAYSEC * DAYSEC / AU_KM / AU_KM / AU_KM // AU³/s/2
const val MU_KM3_S2_TO_AU3_D2 = DAYSEC * DAYSEC / AU_KM / AU_KM / AU_KM
const val MU_AU3_D2_TO_KM3_S2 = (AU_KM * AU_KM * AU_KM) / (DAYSEC * DAYSEC)

/**
 * Heliocentric gravitational constant in meters^3 / second^2, from DE-405.
 */
const val GS = 1.32712440017987E+20
