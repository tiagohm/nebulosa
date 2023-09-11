package nebulosa.api.mounts

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.connection.ConnectionService
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.TrackMode
import nebulosa.math.Angle
import nebulosa.math.Distance.Companion.m
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalTime
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

    @GetMapping("{mountName}")
    fun mount(@PathVariable mountName: String): Mount {
        return requireNotNull(connectionService.mount(mountName))
    }

    @PutMapping("{mountName}/connect")
    fun connect(@PathVariable mountName: String) {
        mountService.connect(mount(mountName))
    }

    @PutMapping("{mountName}/disconnect")
    fun disconnect(@PathVariable mountName: String) {
        mountService.disconnect(mount(mountName))
    }

    @PutMapping("{mountName}/tracking")
    fun tracking(
        @PathVariable mountName: String,
        @RequestParam enabled: Boolean,
    ) {
        mountService.tracking(mount(mountName), enabled)
    }

    @PutMapping("{mountName}/sync")
    fun sync(
        @PathVariable mountName: String,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.sync(mount(mountName), Angle.from(rightAscension, true), Angle.from(declination), j2000)
    }

    @PutMapping("{mountName}/slew-to")
    fun slewTo(
        @PathVariable mountName: String,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.slewTo(mount(mountName), Angle.from(rightAscension, true), Angle.from(declination), j2000)
    }

    @PutMapping("{mountName}/goto")
    fun goTo(
        @PathVariable mountName: String,
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
    ) {
        mountService.goTo(mount(mountName), Angle.from(rightAscension, true), Angle.from(declination), j2000)
    }

    @PutMapping("{mountName}/home")
    fun home(@PathVariable mountName: String) {
        mountService.home(mount(mountName))
    }

    @PutMapping("{mountName}/abort")
    fun abort(@PathVariable mountName: String) {
        mountService.abort(mount(mountName))
    }

    @PutMapping("{mountName}/track-mode")
    fun trackMode(
        @PathVariable mountName: String,
        @RequestParam mode: TrackMode,
    ) {
        mountService.trackMode(mount(mountName), mode)
    }

    @PutMapping("{mountName}/slew-rate")
    fun slewRate(
        @PathVariable mountName: String,
        @RequestParam @Valid @NotBlank rate: String,
    ) {
        val mount = mount(mountName)
        mountService.slewRate(mount, mount.slewRates.first { it.name == rate })
    }

    @PutMapping("{mountName}/move")
    fun move(
        @PathVariable mountName: String,
        @RequestParam direction: GuideDirection,
        @RequestParam enabled: Boolean,
    ) {
        mountService.move(mount(mountName), direction, enabled)
    }

    @PutMapping("{mountName}/park")
    fun park(@PathVariable mountName: String) {
        mountService.park(mount(mountName))
    }

    @PutMapping("{mountName}/unpark")
    fun unpark(@PathVariable mountName: String) {
        mountService.unpark(mount(mountName))
    }

    @PutMapping("{mountName}/coordinates")
    fun coordinates(
        @PathVariable mountName: String,
        @RequestParam @Valid @NotBlank longitude: String,
        @RequestParam @Valid @NotBlank latitude: String,
        @RequestParam(required = false, defaultValue = "0.0") elevation: Double,
    ) {
        mountService.coordinates(mount(mountName), Angle.from(longitude), Angle.from(latitude), elevation.m)
    }

    @PutMapping("{mountName}/datetime")
    fun dateTime(
        @PathVariable mountName: String,
        @RequestParam date: LocalDate,
        @RequestParam time: LocalTime,
        @RequestParam @Valid @Range(min = -720, max = 720) offsetInMinutes: Int,
    ) {
        val dateTime = OffsetDateTime.of(date, time, ZoneOffset.ofTotalSeconds(offsetInMinutes * 60))
        mountService.dateTime(mount(mountName), dateTime)
    }

    @GetMapping("{mountName}/location/zenith")
    fun zenithLocation(@PathVariable mountName: String): ComputedLocation {
        return mountService.computeZenithLocation(mount(mountName))
    }

    @GetMapping("{mountName}/location/celestial-pole/north")
    fun northCelestialPoleLocation(@PathVariable mountName: String): ComputedLocation {
        return mountService.computeNorthCelestialPoleLocation(mount(mountName))
    }

    @GetMapping("{mountName}/location/celestial-pole/south")
    fun southCelestialPoleLocation(@PathVariable mountName: String): ComputedLocation {
        return mountService.computeSouthCelestialPoleLocation(mount(mountName))
    }

    @GetMapping("{mountName}/location/galactic-center")
    fun galacticCenterLocation(@PathVariable mountName: String): ComputedLocation {
        return mountService.computeGalacticCenterLocation(mount(mountName))
    }

    @GetMapping("{mountName}/location")
    fun location(
        @PathVariable mountName: String,
        @RequestParam rightAscension: String,
        @RequestParam declination: String,
        @RequestParam(required = false, defaultValue = "false") j2000: Boolean,
        @RequestParam(required = false, defaultValue = "true") equatorial: Boolean,
        @RequestParam(required = false, defaultValue = "true") horizontal: Boolean,
        @RequestParam(required = false, defaultValue = "true") meridianAt: Boolean,
    ): ComputedLocation {
        val mount = mount(mountName)
        return mountService.computeLocation(
            mount,
            Angle.from(rightAscension, true), Angle.from(declination),
            j2000, equatorial, horizontal, meridianAt,
        )
    }
}
