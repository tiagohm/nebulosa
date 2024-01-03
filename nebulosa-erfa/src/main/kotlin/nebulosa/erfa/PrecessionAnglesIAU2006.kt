package nebulosa.erfa

import nebulosa.math.Angle

data class PrecessionAnglesIAU2006(
    @JvmField val eps0: Angle,
    @JvmField val psia: Angle,
    @JvmField val oma: Angle,
    @JvmField val bpa: Angle,
    @JvmField val bqa: Angle,
    @JvmField val pia: Angle,
    @JvmField val bpia: Angle,
    @JvmField val epsa: Angle,
    @JvmField val chia: Angle,
    @JvmField val za: Angle,
    @JvmField val zetaa: Angle,
    @JvmField val thetaa: Angle,
    @JvmField val pa: Angle,
    @JvmField val gam: Angle,
    @JvmField val phi: Angle,
    @JvmField val psi: Angle,
)
