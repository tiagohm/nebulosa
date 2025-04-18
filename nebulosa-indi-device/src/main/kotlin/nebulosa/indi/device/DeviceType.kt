package nebulosa.indi.device

enum class DeviceType(@JvmField val code: String) {
    CAMERA("CAM"),
    MOUNT("MNT"),
    WHEEL("WHL"),
    FOCUSER("FOC"),
    ROTATOR("ROT"),
    GPS("GPS"),
    DOME("DOM"),
    SWITCH("SWT"),
    GUIDE_OUTPUT("GDT"),
    LIGHT_BOX("LBX"),
    DUST_CAP("DCP"),
}
