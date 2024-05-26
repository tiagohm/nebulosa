import { CameraStartCapture } from './camera.types'

export interface AutoFocusRequest {
    capture: CameraStartCapture
}

export interface AutoFocusPreference {

}

export const EMPTY_AUTO_FOCUS_PREFERENCE: AutoFocusPreference = {

}
