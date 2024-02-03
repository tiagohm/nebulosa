import { CameraStartCapture } from './camera.types'
import { Device } from './device.types'

export type WheelDialogMode = 'CAPTURE' | 'SEQUENCER' | 'FLAT_WIZARD'

export interface FilterWheel extends Device {
    count: number
    position: number
    moving: boolean
}

export const EMPTY_WHEEL: FilterWheel = {
    count: 0,
    position: 0,
    moving: false,
    name: '',
    connected: false
}

export interface WheelDialogInput {
    mode: WheelDialogMode
    wheel: FilterWheel
    request: CameraStartCapture
}

export function wheelPreferenceKey(wheel: FilterWheel) {
    return `wheel.${wheel.name}`
}

export interface WheelPreference {
    shutterPosition?: number
    names?: string[]
    offsets?: number[]
}

export interface FilterSlot {
    position: number
    name: string
    dark: boolean
    offset: number
}
