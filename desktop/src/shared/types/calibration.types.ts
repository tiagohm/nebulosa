import { FrameType } from './camera.types'

export interface CalibrationFrame {
    id: number
    type: FrameType
    name: string
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
    name: string
    key: Omit<CalibrationFrame, 'id' | 'name' | 'path' | 'enabled'>
    frames: CalibrationFrame[]
}

export interface CalibrationPreference {
    openPath?: string
}
