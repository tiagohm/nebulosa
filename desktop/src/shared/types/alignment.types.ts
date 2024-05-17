import { Angle } from './atlas.types'
import { Camera, CameraCaptureEvent, CameraStartCapture } from './camera.types'
import { GuideDirection } from './guider.types'
import { PlateSolverPreference, PlateSolverType } from './settings.types'

export type Hemisphere = 'NORTHERN' | 'SOUTHERN'

export type DARVState = 'IDLE' | 'INITIAL_PAUSE' | 'FORWARD' | 'BACKWARD'

export type TPPAState = 'IDLE' | 'SLEWING' | 'SLEWED' | 'SETTLING' | 'SOLVING' | 'SOLVED' | 'COMPUTED' | 'PAUSING' | 'PAUSED' | 'FINISHED' | 'FAILED'

export type AlignmentMethod = 'DARV' | 'TPPA'

export interface AlignmentPreference {
    darvInitialPause: number
    darvExposureTime: number
    darvHemisphere: Hemisphere
    tppaStartFromCurrentPosition: boolean
    tppaStepDirection: GuideDirection
    tppaCompensateRefraction: boolean
    tppaStopTrackingWhenDone: boolean
    tppaStepDuration: number
    tppaPlateSolverType: PlateSolverType
}

export const EMPTY_ALIGNMENT_PREFERENCE: AlignmentPreference = {
    darvInitialPause: 5,
    darvExposureTime: 30,
    darvHemisphere: 'NORTHERN',
    tppaStartFromCurrentPosition: true,
    tppaStepDirection: 'EAST',
    tppaCompensateRefraction: true,
    tppaStopTrackingWhenDone: true,
    tppaStepDuration: 5,
    tppaPlateSolverType: 'ASTAP',
}

export interface DARVStart {
    capture: CameraStartCapture
    exposureTime: number
    initialPause: number
    direction: GuideDirection
    reversed: boolean
}

export interface DARVEvent extends MessageEvent {
    camera: Camera
    state: DARVState
    direction?: GuideDirection
    capture: CameraCaptureEvent
}

export interface TPPAStart {
    capture: CameraStartCapture
    plateSolver: PlateSolverPreference
    startFromCurrentPosition: boolean
    compensateRefraction: boolean
    stopTrackingWhenDone: boolean
    stepDirection: GuideDirection
    stepDuration: number
    stepSpeed?: string
}

export interface TPPAEvent extends MessageEvent {
    camera: Camera
    state: TPPAState
    rightAscension: Angle
    declination: Angle
    azimuthError: Angle
    altitudeError: Angle
    totalError: Angle
    azimuthErrorDirection: string
    altitudeErrorDirection: string
    capture?: CameraCaptureEvent
}
