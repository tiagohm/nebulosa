package nebulosa.api.mounts

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
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
        return connectionService.mounts().sorted()
    }

    @GetMapping("{mount}")
    fun mount(mount: Mount): Mount {
        return mount
    }

    @PutMapping("{mount}/connect")
    fun connect(mount: Mount) {
        mountService.connect(mount)
    }

    @PutMapping("{mount}/disconnect")
    fun disconnect(mount: Mount) {
        mountService.disconnect(mount)
    }

    @PutMapping("{mount}/tracking")
    fun tracking(
        mount: Mount,
        @RequestParam enabled: Boolean,
    ) {
        mountService.tracking(mount, enabled)
    }

    @PutMapping("{mount}/sync")
    fun sync(
        mount: Mount,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.sync(mount, rightAscension.hours, declination.deg, j2000)
    }

    @PutMapping("{mount}/slew")
    fun slew(
        mount: Mount,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.slewTo(mount, rightAscension.hours, declination.deg, j2000)
    }

    @PutMapping("{mount}/goto")
    fun goTo(
        mount: Mount,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.goTo(mount, rightAscension.hours, declination.deg, j2000)
    }

    @PutMapping("{mount}/home")
    fun home(mount: Mount) {
        mountService.home(mount)
    }

    @PutMapping("{mount}/abort")
    fun abort(mount: Mount) {
        mountService.abort(mount)
    }

    @PutMapping("{mount}/track-mode")
    fun trackMode(
        mount: Mount,
        @RequestParam mode: TrackMode,
    ) {
        mountService.trackMode(mount, mode)
    }

    @PutMapping("{mount}/slew-rate")
    fun slewRate(
        mount: Mount,
        @RequestParam @Valid @NotBlank rate: String,
    ) {
        mountService.slewRate(mount, mount.slewRates.first { it.name == rate })
    }

    @PutMapping("{mount}/move")
    fun move(
        mount: Mount,
        @RequestParam direction: GuideDirection,
        @RequestParam enabled: Boolean,
    ) {
        mountService.move(mount, direction, enabled)
    }

    @PutMapping("{mount}/park")
    fun park(mount: Mount) {
        mountService.park(mount)
    }

    @PutMapping("{mount}/unpark")
    fun unpark(mount: Mount) {
        mountService.unpark(mount)
    }

    @PutMapping("{mount}/coordinates")
    fun coordinates(
        mount: Mount,
        @RequestParam @Valid @NotBlank longitude: String,
        @RequestParam @Valid @NotBlank latitude: String,
        @RequestParam(required = false, defaultValue = "0.0") elevation: Double,
    ) {
        mountService.coordinates(mount, longitude.deg, latitude.deg, elevation.m)
    }

    @PutMapping("{mount}/datetime")
    fun dateTime(
        mount: Mount,
        @DateAndTimeParam dateTime: LocalDateTime,
        @RequestParam @Valid @Range(min = -720, max = 720) offsetInMinutes: Int,
    ) {
        mountService.dateTime(mount, OffsetDateTime.of(dateTime, ZoneOffset.ofTotalSeconds(offsetInMinutes * 60)))
    }

    @GetMapping("{mount}/location/{type}")
    fun celestialLocation(mount: Mount, @PathVariable type: CelestialLocationType): ComputedLocation {
        return when (type) {
            CelestialLocationType.ZENITH -> mountService.computeZenithLocation(mount)
            CelestialLocationType.NORTH_POLE -> mountService.computeNorthCelestialPoleLocation(mount)
            CelestialLocationType.SOUTH_POLE -> mountService.computeSouthCelestialPoleLocation(mount)
            CelestialLocationType.GALACTIC_CENTER -> mountService.computeGalacticCenterLocation(mount)
            CelestialLocationType.MERIDIAN_EQUATOR -> mountService.computeMeridianEquatorLocation(mount)
            CelestialLocationType.MERIDIAN_ECLIPTIC -> mountService.computeMeridianEclipticLocation(mount)
            CelestialLocationType.EQUATOR_ECLIPTIC -> mountService.computeEquatorEclipticLocation(mount)
        }
    }

    @GetMapping("{mount}/location")
    fun location(
        mount: Mount,
        @RequestParam rightAscension: String, @RequestParam declination: String,
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
        mount: Mount,
        @RequestParam path: Path,
        @RequestParam @Valid @PositiveOrZero x: Double,
        @RequestParam @Valid @PositiveOrZero y: Double,
    ) {
        mountService.pointMountHere(mount, path, x, y)
    }

    @PutMapping("{mount}/remote-control/start")
    fun remoteControlStart(
        mount: Mount,
        @RequestParam type: MountRemoteControlType,
        @RequestParam(required = false, defaultValue = "0.0.0.0") host: String,
        @RequestParam(required = false, defaultValue = "10001") @Valid @Positive port: Int,
    ) {
        mountService.remoteControlStart(mount, type, host, port)
    }

    @PutMapping("{mount}/remote-control/stop")
    fun remoteControlStart(mount: Mount, @RequestParam type: MountRemoteControlType) {
        mountService.remoteControlStop(mount, type)
    }

    @GetMapping("{mount}/remote-control")
    fun remoteControlList(mount: Mount): List<MountRemoteControl> {
        return mountService.remoteControlList(mount)
    }
}
