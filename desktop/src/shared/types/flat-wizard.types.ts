import type { CameraCaptureEvent, CameraStartCapture } from './camera.types'

export interface FlatWizardRequest {
	capture: CameraStartCapture
	exposureMin: number
	exposureMax: number
	meanTarget: number
	meanTolerance: number
}

export type FlatWizardState = 'EXPOSURING' | 'CAPTURED' | 'FAILED'

export interface FlatWizardEvent {
	state: FlatWizardState
	exposureTime: number
	capture?: CameraCaptureEvent
	savedPath?: string
}
