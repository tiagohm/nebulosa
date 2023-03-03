package nebulosa.stellarium.protocol

import nebulosa.math.Angle

sealed interface StellariumProtocolMessage {

    data class CurrentPosition(val rightAscension: Angle, val declination: Angle) : StellariumProtocolMessage

    data class Goto(val rightAscension: Angle, val declination: Angle) : StellariumProtocolMessage
}
