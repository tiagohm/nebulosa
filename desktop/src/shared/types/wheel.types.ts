import { CameraStartCapture } from './camera.types'
import { Device } from './device.types'

export type WheelDialogMode = 'CAPTURE' | 'SEQUENCER' | 'FLAT_WIZARD'

export interface FilterWheel extends Device {
    count: number
    position: number
    moving: boolean
    names: string[]
}

export const EMPTY_WHEEL: FilterWheel = {
    sender: '',
    id: '',
    count: 0,
    position: 0,
    moving: false,
    name: '',
    connected: false,
    names: [],
}

export interface WheelDialogInput {
    mode: WheelDialogMode
    wheel: FilterWheel
    request: CameraStartCapture
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
}

export interface WheelRenamed {
    wheel: FilterWheel
    filter: FilterSlot
}

export function isFilterWheel(device?: Device): device is FilterWheel {
    return !!device && 'count' in device
}
