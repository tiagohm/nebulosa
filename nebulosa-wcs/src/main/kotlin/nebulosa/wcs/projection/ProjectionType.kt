package nebulosa.wcs.projection

enum class ProjectionType(@JvmField internal val type: Class<out Projection>? = null) {
    AZP, // Zenithal/azimuthal perspective
    SZP, // Slant zenithal perspective
    TAN(Gnomonic::class.java), // Gnomonic
    STG, // Stereographic
    SIN, // Orthographic/synthesis
    ARC, // Zenithal/azimuthal equidistant
    ZPN, // Zenithal/azimuthal polynomial
    ZEA, // Zenithal/azimuthal equal area
    AIR, // Airy’s projection
    CYP, // Cylindrical perspective
    CEA, // Cylindrical equal area
    CAR, // Plate carrée
    MER, // Mercator’s projection
    COP, // Conic perspective
    COE, // Conic equal area
    COD, // Conic equidistant
    COO, // Conic orthomorphic
    SFL, // Sanson-Flamsteed (“global sinusoid”)
    PAR, // Parabolic
    MOL, // Mollweide’s projection
    AIT, // Hammer-Aitoff
    BON, // Bonne’s projection
    PCO, // Polyconic
    TSC, // Tangential spherical cube
    CSC, // COBE quadrilateralized spherical cube
    QSC, // Quadrilateralized spherical cube
    HPX, // HEALPix
    XPH, // HEALPix polar, aka “butterfly”

    NCP,

    // If built with wcslib 5.0 or later, the following polynomial distortions are supported.
    TPV, // Polynomial distortion
    TUV, // Polynomial distortion
}
