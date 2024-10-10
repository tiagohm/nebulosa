package nebulosa.autofocus

import nebulosa.curve.fitting.CurvePoint

sealed interface AutoFocusResult {

    /**
     * Should take a exposure.
     */
    data object TakeExposure : AutoFocusResult

    /**
     * Should move the focuser to [position], [relative] or not.
     */
    data class MoveFocuser(@JvmField val position: Int, @JvmField val relative: Boolean) : AutoFocusResult

    /**
     * Should call [AutoFocus.determinate] method again.
     */
    data object Determinate : AutoFocusResult

    /**
     * Auto Focus can not be determinated because it failed.
     */
    data class Failed(@JvmField val initialFocusPosition: Int) : AutoFocusResult

    /**
     * Auto Focus finished with success.
     */
    data class Completed(@JvmField val point: CurvePoint) : AutoFocusResult
}
