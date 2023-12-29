import { CameraCaptureEvent } from './camera.types'

export interface FlatWizardEvent {
    exposureTime: number
    capture?: CameraCaptureEvent
    savedPath?: string
}
