package nebulosa.desktop.equipments

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.mounts.*
import java.time.OffsetDateTime

class MountProperty : DeviceProperty<Mount>() {

    @JvmField val isSlewing = SimpleBooleanProperty()
    @JvmField val isTracking = SimpleBooleanProperty()
    @JvmField val isParking = SimpleBooleanProperty()
    @JvmField val isParked = SimpleBooleanProperty()
    @JvmField val slewRates = SimpleListProperty(FXCollections.observableArrayList<String>())
    @JvmField val slewRate = SimpleObjectProperty<String>()
    @JvmField val mountType = SimpleObjectProperty(MountType.EQ_GEM)
    @JvmField val trackModes = SimpleListProperty(FXCollections.observableArrayList<TrackMode>())
    @JvmField val trackMode = SimpleObjectProperty<TrackMode>()
    @JvmField val pierSide = SimpleObjectProperty(PierSide.NEITHER)
    @JvmField val canAbort = SimpleBooleanProperty()
    @JvmField val canSync = SimpleBooleanProperty()
    @JvmField val canPark = SimpleBooleanProperty()
    @JvmField val guideRateWE = SimpleDoubleProperty()
    @JvmField val guideRateNS = SimpleDoubleProperty()
    @JvmField val rightAscension = SimpleDoubleProperty()
    @JvmField val declination = SimpleDoubleProperty()
    @JvmField val rightAscensionJ2000 = SimpleDoubleProperty()
    @JvmField val declinationJ2000 = SimpleDoubleProperty()
    @JvmField val longitude = SimpleDoubleProperty()
    @JvmField val latitude = SimpleDoubleProperty()
    @JvmField val elevation = SimpleDoubleProperty()
    @JvmField val time = SimpleObjectProperty(OffsetDateTime.now())

    override fun changed(prev: Mount?, new: Mount) {
        isSlewing.set(new.isSlewing)
        isTracking.set(new.isTracking)
        slewRates.setAll(new.slewRates)
        slewRate.set(new.slewRate)
        mountType.set(new.mountType)
        trackModes.setAll(new.trackModes)
        trackMode.set(new.trackMode)
        pierSide.set(new.pierSide)
        isParking.set(new.isParking)
        isParked.set(new.isParked)
        canAbort.set(new.canAbort)
        canSync.set(new.canSync)
        canPark.set(new.canPark)
        guideRateWE.set(new.guideRateWE)
        guideRateNS.set(new.guideRateNS)
        rightAscension.set(new.rightAscension.hours)
        declination.set(new.declination.degrees)
        rightAscensionJ2000.set(new.rightAscensionJ2000.hours)
        declinationJ2000.set(new.declinationJ2000.degrees)
        longitude.set(new.longitude.degrees)
        latitude.set(new.latitude.degrees)
        elevation.set(new.elevation.meters)
        time.set(new.time)
    }

    override fun reset() {
        isSlewing.set(false)
        isTracking.set(false)
        slewRates.clear()
        slewRate.set(null)
        mountType.set(MountType.EQ_GEM)
        trackModes.setAll(listOf(TrackMode.SIDEREAL))
        trackMode.set(TrackMode.SIDEREAL)
        pierSide.set(PierSide.NEITHER)
        isParking.set(false)
        isParked.set(false)
        canAbort.set(false)
        canSync.set(false)
        canPark.set(false)
        guideRateWE.set(0.0)
        guideRateNS.set(0.0)
        rightAscension.set(0.0)
        declination.set(0.0)
        rightAscensionJ2000.set(0.0)
        declinationJ2000.set(0.0)
        longitude.set(0.0)
        latitude.set(0.0)
        elevation.set(0.0)
        time.set(OffsetDateTime.now())
    }

    override fun accept(event: DeviceEvent<Mount>) {
        val device = event.device!!

        when (event) {
            is MountSlewingChanged -> isSlewing.set(device.isSlewing)
            is MountSlewRatesChanged -> {
                slewRate.set(null)
                slewRates.setAll(device.slewRates)
                slewRate.set(device.slewRate ?: device.slewRates.firstOrNull())
            }
            is MountSlewRateChanged -> slewRate.set(device.slewRate)
            is MountTypeChanged -> mountType.set(device.mountType)
            is MountTrackModesChanged -> {
                trackMode.set(null)
                trackModes.setAll(device.trackModes)
                trackMode.set(device.trackMode)
            }
            is MountTrackModeChanged -> trackMode.set(device.trackMode)
            is MountTrackingChanged -> isTracking.set(device.isTracking)
            is MountPierSideChanged -> pierSide.set(device.pierSide)
            is MountCanAbortChanged -> canAbort.set(device.canAbort)
            is MountCanSyncChanged -> canSync.set(device.canSync)
            is MountCanParkChanged -> canPark.set(device.canPark)
            is MountEquatorialCoordinatesChanged -> {
                rightAscension.set(device.rightAscension.hours)
                declination.set(device.declination.degrees)
                rightAscensionJ2000.set(device.rightAscensionJ2000.hours)
                declinationJ2000.set(device.declinationJ2000.degrees)
            }
            is MountGuideRateChanged -> {
                guideRateWE.set(device.guideRateWE)
                guideRateNS.set(device.guideRateNS)
            }
            is MountParkChanged -> {
                isParking.set(device.isParking)
                isParked.set(device.isParked)
            }
            is MountCoordinateChanged -> {
                longitude.set(device.longitude.degrees)
                latitude.set(device.latitude.degrees)
                elevation.set(device.elevation.meters)
            }
            is MountTimeChanged -> time.set(device.time)
        }
    }
}
