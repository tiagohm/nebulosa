import { CameraStartCapture } from './camera.types'

export type AutoFocusFittingMode = 'TRENDLINES' | 'PARABOLIC' | 'TREND_PARABOLIC' | 'HYPERBOLIC' | 'TREND_HYPERBOLIC'

export type BacklashCompensationMode = 'NONE' | 'ABSOLUTE' | 'OVERSHOOT'

export interface BacklashCompensation {
    mode: BacklashCompensationMode
    backlashIn: number
    backlashOut: number
}

export interface AutoFocusRequest {
    fittingMode: AutoFocusFittingMode
    capture: CameraStartCapture
    rSquaredThreshold: number
    backlashCompensation: BacklashCompensation
    initialOffsetSteps: number
    stepSize: number
    totalNumberOfAttempts: number
}

export interface AutoFocusPreference extends Omit<AutoFocusRequest, 'capture'> { }

export const EMPTY_AUTO_FOCUS_PREFERENCE: AutoFocusPreference = {
    fittingMode: 'HYPERBOLIC',
    rSquaredThreshold: 0.7,
    initialOffsetSteps: 4,
    stepSize: 100,
    totalNumberOfAttempts: 1,
    backlashCompensation: {
        mode: 'NONE',
        backlashIn: 0,
        backlashOut: 0
    }
}
