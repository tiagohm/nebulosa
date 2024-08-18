package nebulosa.indi.client.device

enum class DeviceInterfaceType(private val code: Int) {
    TELESCOPE(0x0001), // Telescope interface, must subclass INDI::Telescope.
    CCD(0x0002), // CCD interface, must subclass INDI::CCD.
    GUIDER(0x0004), // Guider interface, must subclass INDI::GuiderInterface.
    FOCUSER(0x0008), // Focuser interface, must subclass INDI::FocuserInterface.
    FILTER(0x0010), // Filter interface, must subclass INDI::FilterInterface.
    DOME(0x0020), // Dome interface, must subclass INDI::Dome.
    GPS(0x0040), // GPS interface, must subclass INDI::GPS.
    WEATHER(0x0080), // Weather interface, must subclass INDI::Weather.
    AO(0x0100), // Adaptive Optics Interface.
    DUSTCAP(0x0200), // Dust Cap Interface.
    LIGHTBOX(0x0400), // Light Box Interface.
    DETECTOR(0x0800), // Detector interface, must subclass INDI::Detector.
    ROTATOR(0x1000), // Rotator interface, must subclass INDI::RotatorInterface.
    SPECTROGRAPH(0x2000), // Spectrograph interface.
    CORRELATOR(0x4000), // Correlators (interferometers) interface.
    AUXILIARY(0x8000), // Auxiliary interface.
    OUTPUT(0x10000), // Digital Output (e.g. Relay) interface.
    INPUT(0x20000), // Digital/Analog Input (e.g. GPIO) interface.
    POWER(0x40000); // Auxiliary interface.

    companion object {

        @JvmStatic
        fun isMount(value: Int) = (value and TELESCOPE.code) != 0

        @JvmStatic
        fun isCamera(value: Int) = (value and CCD.code) != 0

        @JvmStatic
        fun isGuider(value: Int) = (value and GUIDER.code) != 0

        @JvmStatic
        fun isFocuser(value: Int) = (value and FOCUSER.code) != 0

        @JvmStatic
        fun isFilterWheel(value: Int) = (value and FILTER.code) != 0

        @JvmStatic
        fun isDome(value: Int) = (value and DOME.code) != 0

        @JvmStatic
        fun isGPS(value: Int) = (value and GPS.code) != 0

        @JvmStatic
        fun isWeather(value: Int) = (value and WEATHER.code) != 0

        @JvmStatic
        fun isAO(value: Int) = (value and AO.code) != 0

        @JvmStatic
        fun isDustCap(value: Int) = (value and DUSTCAP.code) != 0

        @JvmStatic
        fun isLightBox(value: Int) = (value and LIGHTBOX.code) != 0

        @JvmStatic
        fun isDetector(value: Int) = (value and DETECTOR.code) != 0

        @JvmStatic
        fun isRotator(value: Int) = (value and ROTATOR.code) != 0

        @JvmStatic
        fun isSpectrograph(value: Int) = (value and SPECTROGRAPH.code) != 0

        @JvmStatic
        fun isCorrelator(value: Int) = (value and CORRELATOR.code) != 0

        @JvmStatic
        fun isAuxiliary(value: Int) = (value and AUXILIARY.code) != 0

        @JvmStatic
        fun isOutput(value: Int) = (value and OUTPUT.code) != 0

        @JvmStatic
        fun isInput(value: Int) = (value and INPUT.code) != 0

        @JvmStatic
        fun isPower(value: Int) = (value and POWER.code) != 0

        @JvmStatic
        fun isSensor(value: Int) = isSpectrograph(value) && isDetector(value) && isCorrelator(value)
    }
}
