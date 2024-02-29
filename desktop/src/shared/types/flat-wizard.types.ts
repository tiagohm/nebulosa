import { CameraCaptureElapsed, CameraStartCapture } from './camera.types'

export interface FlatWizardRequest {
    captureRequest: CameraStartCapture
    exposureMin: number
    exposureMax: number
    meanTarget: number
    meanTolerance: number
}

export type FlatWizardState = 'EXPOSURING' | 'CAPTURED' | 'FAILED'

export interface FlatWizardElapsed {
    state: FlatWizardState
    exposureTime: number
    capture?: CameraCaptureElapsed
    savedPath?: string
    message?: string
}
