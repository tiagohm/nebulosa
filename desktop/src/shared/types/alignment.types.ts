import { Camera, CameraStartCapture } from './camera.types'
import { GuideDirection, GuideOutput } from './guider.types'
import { Mount } from './mount.types'
import { PlateSolverOptions, PlateSolverType } from './settings.types'

export type Hemisphere = 'NORTHERN' | 'SOUTHERN'

export type DARVState = 'IDLE' | 'INITIAL_PAUSE' | 'FORWARD' | 'BACKWARD'

export type TPPAState = 'IDLE' | 'SLEWING' | 'SOLVING' | 'SOLVED' | 'COMPUTED' | 'FAILED' | 'FINISHED'

export type AlignmentMethod = 'DARV' | 'TPPA'

export interface AlignmentPreference {
    darvInitialPause: number
    darvExposureTime: number
    darvHemisphere: Hemisphere
    tppaStartFromCurrentPosition: boolean
    tppaEastDirection: boolean
    tppaRefractionAdjustment: boolean
    tppaStopTrackingWhenDone: boolean
    tppaStepDistance: number
    tppaPlateSolverType: PlateSolverType
}

export const EMPTY_ALIGNMENT_PREFERENCE: AlignmentPreference = {
    darvInitialPause: 5,
    darvExposureTime: 30,
    darvHemisphere: 'NORTHERN',
    tppaStartFromCurrentPosition: true,
    tppaEastDirection: true,
    tppaRefractionAdjustment: true,
    tppaStopTrackingWhenDone: true,
    tppaStepDistance: 10,
    tppaPlateSolverType: 'ASTAP',
}

export interface DARVStart {
    capture: CameraStartCapture
    exposureTime: number
    initialPause: number
    direction: GuideDirection
    reversed: boolean
}

export interface DARVElapsed extends MessageEvent {
    camera: Camera
    guideOutput: GuideOutput
    remainingTime: number
    progress: number
    state: DARVState
    direction?: GuideDirection
}

export interface TPPAStart {
    capture: CameraStartCapture
    plateSolver: PlateSolverOptions
    startFromCurrentPosition: boolean
    eastDirection: boolean
    refractionAdjustment: boolean
    stopTrackingWhenDone: boolean
    stepDistance: number
}

export interface TPPAElapsed extends MessageEvent {
    camera: Camera
    mount?: Mount
    elapsedTime: number
    stepCount: number
    state: TPPAState
    rightAscension: number
    declination: number
    azimuth: number
    altitude: number
}
