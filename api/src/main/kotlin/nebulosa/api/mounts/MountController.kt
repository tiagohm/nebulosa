package nebulosa.api.mounts

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.beans.annotations.DateAndTime
import nebulosa.api.beans.annotations.EntityBy
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
    fun mount(@EntityBy mount: Mount): Mount {
        return mount
    }

    @PutMapping("{mount}/connect")
    fun connect(@EntityBy mount: Mount) {
        mountService.connect(mount)
    }

    @PutMapping("{mount}/disconnect")
    fun disconnect(@EntityBy mount: Mount) {
        mountService.disconnect(mount)
    }

    @PutMapping("{mount}/tracking")
    fun tracking(
        @EntityBy mount: Mount,
        @RequestParam enabled: Boolean,
    ) {
        mountService.tracking(mount, enabled)
    }

    @PutMapping("{mount}/sync")
    fun sync(
        @EntityBy mount: Mount,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.sync(mount, rightAscension.hours, declination.deg, j2000)
    }

    @PutMapping("{mount}/slew-to")
    fun slewTo(
        @EntityBy mount: Mount,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.slewTo(mount, rightAscension.hours, declination.deg, j2000)
    }

    @PutMapping("{mount}/goto")
    fun goTo(
        @EntityBy mount: Mount,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.goTo(mount, rightAscension.hours, declination.deg, j2000)
    }

    @PutMapping("{mount}/home")
    fun home(@EntityBy mount: Mount) {
        mountService.home(mount)
    }

    @PutMapping("{mount}/abort")
    fun abort(@EntityBy mount: Mount) {
        mountService.abort(mount)
    }

    @PutMapping("{mount}/track-mode")
    fun trackMode(
        @EntityBy mount: Mount,
        @RequestParam mode: TrackMode,
    ) {
        mountService.trackMode(mount, mode)
    }

    @PutMapping("{mount}/slew-rate")
    fun slewRate(
        @EntityBy mount: Mount,
        @RequestParam @Valid @NotBlank rate: String,
    ) {
        mountService.slewRate(mount, mount.slewRates.first { it.name == rate })
    }

    @PutMapping("{mount}/move")
    fun move(
        @EntityBy mount: Mount,
        @RequestParam direction: GuideDirection,
        @RequestParam enabled: Boolean,
    ) {
        mountService.move(mount, direction, enabled)
    }

    @PutMapping("{mount}/park")
    fun park(@EntityBy mount: Mount) {
        mountService.park(mount)
    }

    @PutMapping("{mount}/unpark")
    fun unpark(@EntityBy mount: Mount) {
        mountService.unpark(mount)
    }

    @PutMapping("{mount}/coordinates")
    fun coordinates(
        @EntityBy mount: Mount,
        @RequestParam @Valid @NotBlank longitude: String,
        @RequestParam @Valid @NotBlank latitude: String,
        @RequestParam(required = false, defaultValue = "0.0") elevation: Double,
    ) {
        mountService.coordinates(mount, longitude.deg, latitude.deg, elevation.m)
    }

    @PutMapping("{mount}/datetime")
    fun dateTime(
        @EntityBy mount: Mount,
        @DateAndTime dateTime: LocalDateTime,
        @RequestParam @Valid @Range(min = -720, max = 720) offsetInMinutes: Int,
    ) {
        mountService.dateTime(mount, OffsetDateTime.of(dateTime, ZoneOffset.ofTotalSeconds(offsetInMinutes * 60)))
    }

    @GetMapping("{mount}/location/zenith")
    fun zenithLocation(@EntityBy mount: Mount): ComputedLocation {
        return mountService.computeZenithLocation(mount)
    }

    @GetMapping("{mount}/location/celestial-pole/north")
    fun northCelestialPoleLocation(@EntityBy mount: Mount): ComputedLocation {
        return mountService.computeNorthCelestialPoleLocation(mount)
    }

    @GetMapping("{mount}/location/celestial-pole/south")
    fun southCelestialPoleLocation(@EntityBy mount: Mount): ComputedLocation {
        return mountService.computeSouthCelestialPoleLocation(mount)
    }

    @GetMapping("{mount}/location/galactic-center")
    fun galacticCenterLocation(@EntityBy mount: Mount): ComputedLocation {
        return mountService.computeGalacticCenterLocation(mount)
    }

    @GetMapping("{mount}/location")
    fun location(
        @EntityBy mount: Mount,
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
        @EntityBy mount: Mount,
        @RequestParam path: Path,
        @RequestParam @Valid @PositiveOrZero x: Double,
        @RequestParam @Valid @PositiveOrZero y: Double,
        @RequestParam(required = false, defaultValue = "true") synchronized: Boolean,
    ) {
        mountService.pointMountHere(mount, path, x, y, synchronized)
    }
}
