import { Angle } from './atlas.types'
import { Camera, CameraStartCapture } from './camera.types'
import { GuideDirection } from './guider.types'
import { PlateSolverPreference, PlateSolverType } from './settings.types'

export type Hemisphere = 'NORTHERN' | 'SOUTHERN'

export type DARVState = 'IDLE' | 'INITIAL_PAUSE' | 'FORWARD' | 'BACKWARD'

export type TPPAState = 'IDLE' | 'SLEWING' | 'SOLVING' | 'SOLVED' | 'PAUSING' | 'PAUSED' | 'COMPUTED' | 'FAILED' | 'FINISHED'

export type AlignmentMethod = 'DARV' | 'TPPA'

export interface AlignmentPreference {
    darvInitialPause: number
    darvExposureTime: number
    darvHemisphere: Hemisphere
    tppaStartFromCurrentPosition: boolean
    tppaEastDirection: boolean
    tppaCompensateRefraction: boolean
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
    tppaCompensateRefraction: true,
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
    remainingTime: number
    progress: number
    state: DARVState
    direction?: GuideDirection
}

export interface TPPAStart {
    capture: CameraStartCapture
    plateSolver: PlateSolverPreference
    startFromCurrentPosition: boolean
    eastDirection: boolean
    compensateRefraction: boolean
    stopTrackingWhenDone: boolean
    stepDistance: number
}

export interface TPPAElapsed extends MessageEvent {
    camera: Camera
    elapsedTime: number
    stepCount: number
    state: TPPAState
    rightAscension: Angle
    declination: Angle
    azimuthError: Angle
    altitudeError: Angle
    totalError: Angle
    azimuthErrorDirection: string
    altitudeErrorDirection: string
}
