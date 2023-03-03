package nebulosa.lx200.protocol

import nebulosa.math.Angle
import java.time.LocalDate
import java.time.LocalTime

sealed interface LX200ProtocolMessage {

    object Ack : LX200ProtocolMessage

    object Ok : LX200ProtocolMessage

    object Zero : LX200ProtocolMessage

    data class RAPosition(val rightAscension: Angle) : LX200ProtocolMessage

    data class DECPosition(val declination: Angle) : LX200ProtocolMessage

    data class Longitude(val longitude: Angle) : LX200ProtocolMessage

    data class Latitude(val latitude: Angle) : LX200ProtocolMessage

    data class Date(val date: LocalDate) : LX200ProtocolMessage

    data class Time(val time: LocalTime) : LX200ProtocolMessage

    data class ZoneOffset(val offset: Double) : LX200ProtocolMessage

    data class Slewing(val slewing: Boolean) : LX200ProtocolMessage

    // https://www.cloudynights.com/topic/72166-lx-200-gps-serial-commands/
    data class Status(val type: String, val tracking: Boolean) : LX200ProtocolMessage
}
