import { AutoSubFolderMode, Camera, CameraCaptureElapsed, CameraStartCapture, Dither } from './camera.types'
import { Focuser } from './focuser.types'
import { FilterWheel } from './wheel.types'

export type SequenceCaptureMode = 'FULLY' | 'INTERLEAVED'

export interface AutoFocusAfterConditions {
    enabled: boolean
    onStart: boolean
    onFilterChange: boolean
    afterElapsedTime: number
    afterElapsedTimeEnabled: boolean
    afterExposures: number
    afterExposuresEnabled: boolean
    afterTemperatureChange: number
    afterTemperatureChangeEnabled: boolean
    afterHFDIncrease: number
    afterHFDIncreaseEnabled: boolean
}

export interface SequencePlan {
    initialDelay: number
    captureMode: SequenceCaptureMode
    autoSubFolderMode: AutoSubFolderMode
    savePath?: string
    entries: CameraStartCapture[]
    dither: Dither
    autoFocus: AutoFocusAfterConditions
    camera?: Camera
    wheel?: FilterWheel
    focuser?: Focuser
}

export const EMPTY_SEQUENCE_PLAN: SequencePlan = {
    initialDelay: 0,
    captureMode: 'FULLY',
    autoSubFolderMode: 'OFF',
    entries: [],
    dither: {
        enabled: false,
        amount: 1.5,
        raOnly: false,
        afterExposures: 1
    },
    autoFocus: {
        enabled: false,
        onStart: false,
        onFilterChange: false,
        afterElapsedTime: 1800, // 30 min
        afterExposures: 10,
        afterTemperatureChange: 5,
        afterHFDIncrease: 10,
        afterElapsedTimeEnabled: false,
        afterExposuresEnabled: false,
        afterTemperatureChangeEnabled: false,
        afterHFDIncreaseEnabled: false
    },
}

export interface SequencerElapsed extends MessageEvent {
    id: number
    elapsedTime: number
    remainingTime: number
    progress: number
    capture?: CameraCaptureElapsed
}
