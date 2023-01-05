package nebulosa.desktop.equipments

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.mounts.*

class MountProperty : DeviceProperty<Mount>() {

    @JvmField val isTracking = SimpleBooleanProperty()
    @JvmField val isParking = SimpleBooleanProperty()
    @JvmField val isParked = SimpleBooleanProperty()
    @JvmField val slewRates = SimpleListProperty(FXCollections.observableArrayList<SlewRate>())
    @JvmField val slewRate = SimpleObjectProperty<SlewRate>()
    @JvmField val mountType = SimpleObjectProperty(MountType.EQ_GEM)
    @JvmField val trackModes = SimpleListProperty(FXCollections.observableArrayList<TrackMode>())
    @JvmField val trackMode = SimpleObjectProperty(TrackMode.SIDEREAL)
    @JvmField val pierSide = SimpleObjectProperty(PierSide.NEITHER)
    @JvmField val canAbort = SimpleBooleanProperty()
    @JvmField val canSync = SimpleBooleanProperty()
    @JvmField val guideRateWE = SimpleDoubleProperty()
    @JvmField val guideRateNS = SimpleDoubleProperty()

    override fun changed(value: Mount) {
        isTracking.set(value.isTracking)
        slewRates.setAll(value.slewRates)
        slewRate.set(value.slewRate)
        mountType.set(value.mountType)
        trackModes.setAll(value.trackModes)
        trackMode.set(value.trackMode)
        pierSide.set(value.pierSide)
        isParking.set(value.isParking)
        isParked.set(value.isParked)
        canAbort.set(value.canAbort)
        canSync.set(value.canSync)
        guideRateWE.set(value.guideRateWE)
        guideRateNS.set(value.guideRateNS)
    }

    override fun reset() {
        isTracking.set(false)
        slewRates.clear()
        slewRate.set(null)
        mountType.set(MountType.EQ_GEM)
        trackModes.clear()
        trackMode.set(TrackMode.SIDEREAL)
        pierSide.set(PierSide.NEITHER)
        isParking.set(false)
        isParked.set(false)
        canAbort.set(false)
        canSync.set(false)
        guideRateWE.set(0.0)
        guideRateNS.set(0.0)
    }

    override fun accept(event: DeviceEvent<*>) {
        when (event) {
            is MountSlewRatesChanged -> Platform.runLater { slewRates.setAll(value.slewRates) }
            is MountSlewRateChanged -> Platform.runLater { slewRate.set(value.slewRate) }
            is MountTypeChanged -> Platform.runLater { mountType.set(value.mountType) }
            is MountTrackModeChanged -> Platform.runLater { trackMode.set(value.trackMode) }
            is MountTrackModesChanged -> Platform.runLater { trackModes.setAll(value.trackModes) }
            is MountTrackingChanged -> Platform.runLater { isTracking.set(value.isTracking) }
            is MountPierSideChanged -> Platform.runLater { pierSide.set(value.pierSide) }
            is MountCanAbortChanged -> Platform.runLater { canAbort.set(value.canAbort) }
            is MountCanSyncChanged -> Platform.runLater { canSync.set(value.canSync) }
            is MountGuideRateChanged -> Platform.runLater {
                guideRateWE.set(value.guideRateWE)
                guideRateNS.set(value.guideRateNS)
            }
            is MountParkChanged -> Platform.runLater {
                isParking.set(value.isParking)
                isParked.set(value.isParked)
            }
        }
    }
}
