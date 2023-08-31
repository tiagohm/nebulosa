package nebulosa.api.mounts

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.connection.ConnectionService
import nebulosa.api.data.responses.ComputedCoordinateResponse
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.TrackMode
import nebulosa.math.Angle
import nebulosa.math.Distance.Companion.m
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestController
class MountController(
    private val connectionService: ConnectionService,
    private val mountService: MountService,
) {

    @GetMapping("attachedMounts")
    fun attachedMounts(): List<Mount> {
        return connectionService.mounts()
    }

    @GetMapping("mount")
    fun mount(@RequestParam @Valid @NotBlank name: String): Mount {
        return requireNotNull(connectionService.mount(name))
    }

    @PostMapping("mountConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.connect(mount)
    }

    @PostMapping("mountDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.disconnect(mount)
    }

    @PostMapping("mountTracking")
    fun tracking(
        @RequestParam @Valid @NotBlank name: String,
        enable: Boolean,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.tracking(mount, enable)
    }

    @PostMapping("mountSync")
    fun sync(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.sync(mount, Angle.from(rightAscension, true), Angle.from(declination), j2000)
    }

    @PostMapping("mountSlewTo")
    fun slewTo(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.slewTo(mount, Angle.from(rightAscension, true), Angle.from(declination), j2000)
    }

    @PostMapping("mountGoTo")
    fun goTo(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.goTo(mount, Angle.from(rightAscension, true), Angle.from(declination), j2000)
    }

    @PostMapping("mountHome")
    fun home(@RequestParam @Valid @NotBlank name: String) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.home(mount)
    }

    @PostMapping("mountAbort")
    fun abort(@RequestParam @Valid @NotBlank name: String) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.abort(mount)
    }

    @PostMapping("mountTrackMode")
    fun trackMode(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam mode: TrackMode,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.trackMode(mount, mode)
    }

    @PostMapping("mountSlewRate")
    fun slewRate(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @NotBlank rate: String,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.slewRate(mount, mount.slewRates.first { it.name == rate })
    }

    @PostMapping("mountMoveNorth")
    fun moveNorth(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam enable: Boolean,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.moveNorth(mount, enable)
    }

    @PostMapping("mountMoveSouth")
    fun moveSouth(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam enable: Boolean,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.moveSouth(mount, enable)
    }

    @PostMapping("mountMoveWest")
    fun moveWest(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam enable: Boolean,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.moveWest(mount, enable)
    }

    @PostMapping("mountMoveEast")
    fun moveEast(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam enable: Boolean,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.moveEast(mount, enable)
    }

    @PostMapping("mountPark")
    fun park(@RequestParam @Valid @NotBlank name: String) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.park(mount)
    }

    @PostMapping("mountUnpark")
    fun unpark(@RequestParam @Valid @NotBlank name: String) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.unpark(mount)
    }

    @PostMapping("mountCoordinates")
    fun coordinates(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @NotBlank longitude: String,
        @RequestParam @Valid @NotBlank latitude: String,
        @RequestParam(required = false, defaultValue = "0.0") elevation: Double,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        mountService.coordinates(mount, Angle.from(longitude), Angle.from(latitude), elevation.m)
    }

    @PostMapping("mountDateTime")
    fun dateTime(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam date: LocalDate,
        @RequestParam time: LocalTime,
        @RequestParam @Valid @Range(min = -720, max = 720) offsetInMinutes: Int,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        val dateTime = OffsetDateTime.of(date, time, ZoneOffset.ofTotalSeconds(offsetInMinutes * 60))
        mountService.dateTime(mount, dateTime)
    }

    @GetMapping("mountZenithLocation")
    fun zenithLocation(@RequestParam @Valid @NotBlank name: String): ComputedCoordinateResponse {
        val mount = requireNotNull(connectionService.mount(name))
        return mountService.computeCoordinates(
            mount, mountService.computeLST(mount), mount.latitude,
            j2000 = false, equatorial = true, horizontal = true, meridian = false,
        )
    }

    @GetMapping("mountNorthCelestialPoleLocation")
    fun northCelestialPoleLocation(@RequestParam @Valid @NotBlank name: String): ComputedCoordinateResponse {
        val mount = requireNotNull(connectionService.mount(name))
        return mountService.computeCoordinates(
            mount, mountService.computeLST(mount), Angle.QUARTER,
            j2000 = false, equatorial = true, horizontal = true, meridian = false,
        )
    }

    @GetMapping("mountSouthCelestialPoleLocation")
    fun southCelestialPoleLocation(@RequestParam @Valid @NotBlank name: String): ComputedCoordinateResponse {
        val mount = requireNotNull(connectionService.mount(name))
        return mountService.computeCoordinates(
            mount, mountService.computeLST(mount), -Angle.QUARTER,
            j2000 = false, equatorial = true, horizontal = true, meridian = false,
        )
    }

    @GetMapping("mountGalacticCenterLocation")
    fun galacticCenterLocation(@RequestParam @Valid @NotBlank name: String): ComputedCoordinateResponse {
        val mount = requireNotNull(connectionService.mount(name))
        return mountService.computeCoordinates(
            mount, GALACTIC_CENTER_RA, GALACTIC_CENTER_DEC,
            j2000 = true, equatorial = true, horizontal = true, meridian = false,
        )
    }

    @GetMapping("mountComputeCoordinates")
    fun computeCoordinates(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam(required = false) rightAscension: String?,
        @RequestParam(required = false) declination: String?,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
        @RequestParam(required = false, defaultValue = "true") equatorial: Boolean,
        @RequestParam(required = false, defaultValue = "true") horizontal: Boolean,
        @RequestParam(required = false, defaultValue = "true") meridian: Boolean,
    ): ComputedCoordinateResponse {
        val mount = requireNotNull(connectionService.mount(name))
        return mountService.computeCoordinates(
            mount,
            Angle.from(rightAscension, true, defaultValue = mount.rightAscension),
            Angle.from(declination, defaultValue = mount.declination),
            j2000, equatorial, horizontal, meridian,
        )
    }

    companion object {

        @JvmStatic private val GALACTIC_CENTER_RA = Angle.from("17 45 40.04", true)
        @JvmStatic private val GALACTIC_CENTER_DEC = Angle.from("-29 00 28.1")
    }
}
