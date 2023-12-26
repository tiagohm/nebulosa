package nebulosa.api.mounts

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.beans.converters.indi.DeviceOrEntityParam
import nebulosa.api.beans.converters.time.DateAndTimeParam
import nebulosa.api.connection.ConnectionService
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.TrackMode
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.math.m
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestController
@RequestMapping("mounts")
class MountController(
    private val connectionService: ConnectionService,
    private val mountService: MountService,
) {

    @GetMapping
    fun mounts(): List<Mount> {
        return connectionService.mounts()
    }

    @GetMapping("{mount}")
    fun mount(@DeviceOrEntityParam mount: Mount): Mount {
        return mount
    }

    @PutMapping("{mount}/connect")
    fun connect(@DeviceOrEntityParam mount: Mount) {
        mountService.connect(mount)
    }

    @PutMapping("{mount}/disconnect")
    fun disconnect(@DeviceOrEntityParam mount: Mount) {
        mountService.disconnect(mount)
    }

    @PutMapping("{mount}/tracking")
    fun tracking(
        @DeviceOrEntityParam mount: Mount,
        @RequestParam enabled: Boolean,
    ) {
        mountService.tracking(mount, enabled)
    }

    @PutMapping("{mount}/sync")
    fun sync(
        @DeviceOrEntityParam mount: Mount,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.sync(mount, rightAscension.hours, declination.deg, j2000)
    }

    @PutMapping("{mount}/slew")
    fun slew(
        @DeviceOrEntityParam mount: Mount,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.slewTo(mount, rightAscension.hours, declination.deg, j2000)
    }

    @PutMapping("{mount}/goto")
    fun goTo(
        @DeviceOrEntityParam mount: Mount,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.goTo(mount, rightAscension.hours, declination.deg, j2000)
    }

    @PutMapping("{mount}/home")
    fun home(@DeviceOrEntityParam mount: Mount) {
        mountService.home(mount)
    }

    @PutMapping("{mount}/abort")
    fun abort(@DeviceOrEntityParam mount: Mount) {
        mountService.abort(mount)
    }

    @PutMapping("{mount}/track-mode")
    fun trackMode(
        @DeviceOrEntityParam mount: Mount,
        @RequestParam mode: TrackMode,
    ) {
        mountService.trackMode(mount, mode)
    }

    @PutMapping("{mount}/slew-rate")
    fun slewRate(
        @DeviceOrEntityParam mount: Mount,
        @RequestParam @Valid @NotBlank rate: String,
    ) {
        mountService.slewRate(mount, mount.slewRates.first { it.name == rate })
    }

    @PutMapping("{mount}/move")
    fun move(
        @DeviceOrEntityParam mount: Mount,
        @RequestParam direction: GuideDirection,
        @RequestParam enabled: Boolean,
    ) {
        mountService.move(mount, direction, enabled)
    }

    @PutMapping("{mount}/park")
    fun park(@DeviceOrEntityParam mount: Mount) {
        mountService.park(mount)
    }

    @PutMapping("{mount}/unpark")
    fun unpark(@DeviceOrEntityParam mount: Mount) {
        mountService.unpark(mount)
    }

    @PutMapping("{mount}/coordinates")
    fun coordinates(
        @DeviceOrEntityParam mount: Mount,
        @RequestParam @Valid @NotBlank longitude: String,
        @RequestParam @Valid @NotBlank latitude: String,
        @RequestParam(required = false, defaultValue = "0.0") elevation: Double,
    ) {
        mountService.coordinates(mount, longitude.deg, latitude.deg, elevation.m)
    }

    @PutMapping("{mount}/datetime")
    fun dateTime(
        @DeviceOrEntityParam mount: Mount,
        @DateAndTimeParam dateTime: LocalDateTime,
        @RequestParam @Valid @Range(min = -720, max = 720) offsetInMinutes: Int,
    ) {
        mountService.dateTime(mount, OffsetDateTime.of(dateTime, ZoneOffset.ofTotalSeconds(offsetInMinutes * 60)))
    }

    @GetMapping("{mount}/location/zenith")
    fun zenithLocation(@DeviceOrEntityParam mount: Mount): ComputedLocation {
        return mountService.computeZenithLocation(mount)
    }

    @GetMapping("{mount}/location/celestial-pole/north")
    fun northCelestialPoleLocation(@DeviceOrEntityParam mount: Mount): ComputedLocation {
        return mountService.computeNorthCelestialPoleLocation(mount)
    }

    @GetMapping("{mount}/location/celestial-pole/south")
    fun southCelestialPoleLocation(@DeviceOrEntityParam mount: Mount): ComputedLocation {
        return mountService.computeSouthCelestialPoleLocation(mount)
    }

    @GetMapping("{mount}/location/galactic-center")
    fun galacticCenterLocation(@DeviceOrEntityParam mount: Mount): ComputedLocation {
        return mountService.computeGalacticCenterLocation(mount)
    }

    @GetMapping("{mount}/location")
    fun location(
        @DeviceOrEntityParam mount: Mount,
        @RequestParam rightAscension: String,
        @RequestParam declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
        @RequestParam(required = false, defaultValue = "true") equatorial: Boolean,
        @RequestParam(required = false, defaultValue = "true") horizontal: Boolean,
        @RequestParam(required = false, defaultValue = "true") meridianAt: Boolean,
    ): ComputedLocation {
        return mountService.computeLocation(
            mount, rightAscension.hours, declination.deg,
            j2000, equatorial, horizontal, meridianAt,
        )
    }

    @PutMapping("{mount}/point-here")
    fun pointMountHere(
        @DeviceOrEntityParam mount: Mount,
        @RequestParam path: Path,
        @RequestParam @Valid @PositiveOrZero x: Double,
        @RequestParam @Valid @PositiveOrZero y: Double,
        @RequestParam(required = false, defaultValue = "true") synchronized: Boolean,
    ) {
        mountService.pointMountHere(mount, path, x, y, synchronized)
    }
}
