import { Camera, CameraStartCapture } from './camera.types'
import { GuideDirection, GuideOutput } from './guider.types'

export type Hemisphere = 'NORTHERN' | 'SOUTHERN'

export type DARVState = 'IDLE' | 'INITIAL_PAUSE' | 'FORWARD' | 'BACKWARD'

export interface DARVStart {
    capture?: CameraStartCapture
    exposureTime: number
    initialPause: number
    direction: GuideDirection
    reversed: boolean
}

export interface DARVEvent extends MessageEvent {
    camera: Camera
    guideOutput: GuideOutput
    remainingTime: number
    progress: number
    state: DARVState
    direction?: GuideDirection
}
