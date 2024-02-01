import { CameraCaptureEvent, CameraStartCapture } from './camera.types'

export interface FlatWizardRequest {
    captureRequest: CameraStartCapture
    exposureMin: number
    exposureMax: number
    meanTarget: number
    meanTolerance: number
}

export interface FlatWizardEvent {
    exposureTime: number
    capture?: CameraCaptureEvent
    savedPath?: string
}