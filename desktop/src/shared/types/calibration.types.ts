import { FrameType } from './camera.types'

export interface CalibrationFrame {
    id: number
    type: FrameType
    camera: string
    filter?: string
    exposureTime: number
    temperature: number
    width: number
    height: number
    binX: number
    binY: number
    gain: number
    path: string
    enabled: boolean
}

export interface CalibrationFrameGroup {
    id: number
    key: Omit<CalibrationFrame, 'id' | 'camera' | 'path' | 'enabled'>
    frames: CalibrationFrame[]
}

export interface CalibrationPreference {
    openPath?: string
}
