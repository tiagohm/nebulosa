package nebulosa.desktop.core.scene.image

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import nebulosa.io.resource

open class Icon(name: String) : Image(resource("icons/$name.png"), 24.0, 24.0, true, true) {

    val view get() = ImageView(this)

    companion object {

        @JvmStatic val arrowBottomLeftThick = Icon("arrow-bottom-left-thick")
        @JvmStatic val arrowBottomRightThick = Icon("arrow-bottom-right-thick")
        @JvmStatic val arrowDownThick = Icon("arrow-down-thick")
        @JvmStatic val arrowLeftThick = Icon("arrow-left-thick")
        @JvmStatic val arrowRightThick = Icon("arrow-right-thick")
        @JvmStatic val arrowTopLeftThick = Icon("arrow-top-left-thick")
        @JvmStatic val arrowTopRightThick = Icon("arrow-top-right-thick")
        @JvmStatic val arrowUpThick = Icon("arrow-up-thick")
        @JvmStatic val bahtinov = Icon("bahtinov")
        @JvmStatic val cameraIris = Icon("camera-iris")
        @JvmStatic val check = Icon("check")
        @JvmStatic val closeCircle = Icon("close-circle")
        @JvmStatic val connection = Icon("connection")
        @JvmStatic val crosshairsGps = Icon("crosshairs-gps")
        @JvmStatic val dome = Icon("dome")
        @JvmStatic val dotsVertical = Icon("dots-vertical")
        @JvmStatic val focuser = Icon("focuser")
        @JvmStatic val folder = Icon("folder")
        @JvmStatic val fullscreen = Icon("fullscreen")
        @JvmStatic val fw = Icon("fw")
        @JvmStatic val guider = Icon("guider")
        @JvmStatic val image = Icon("image")
        @JvmStatic val imageFilterCenterFocus = Icon("image-filter-center-focus")
        @JvmStatic val information = Icon("information")
        @JvmStatic val map = Icon("map")
        @JvmStatic val nebulosa = Icon("nebulosa")
        @JvmStatic val nebulosaCameraManager = Icon("nebulosa-camera-manager")
        @JvmStatic val nebulosaFocuserManager = Icon("nebulosa-focuser-manager")
        @JvmStatic val nebulosaFwManager = Icon("nebulosa-fw-manager")
        @JvmStatic val nebulosaImageStretcher = Icon("nebulosa-image-stretcher")
        @JvmStatic val nebulosaImageViewer = Icon("nebulosa-image-viewer")
        @JvmStatic val nebulosaMountManager = Icon("nebulosa-mount-manager")
        @JvmStatic val nebulosaPlateSolver = Icon("nebulosa-plate-solver")
        @JvmStatic val nebulosaScnr = Icon("nebulosa-scnr")
        @JvmStatic val nebulosaStellarium = Icon("nebulosa-stellarium")
        @JvmStatic val platesolving = Icon("platesolving")
        @JvmStatic val play = Icon("play")
        @JvmStatic val remoteTv = Icon("remote-tv")
        @JvmStatic val rotator = Icon("rotator")
        @JvmStatic val stop = Icon("stop")
        @JvmStatic val sunrise = Icon("sunrise")
        @JvmStatic val switches = Icon("switches")
        @JvmStatic val sync = Icon("sync")
        @JvmStatic val tableClock = Icon("table-clock")
        @JvmStatic val telescope = Icon("telescope")
        @JvmStatic val web = Icon("web")
    }
}
