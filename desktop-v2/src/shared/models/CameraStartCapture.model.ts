import { FrameType } from '../types'

export interface CameraStartCapture {
    exposure: number
    amount: number
    delay: number
    x: number
    y: number
    width: number
    height: number
    frameFormat?: string
    frameType: FrameType
    binX: number
    binY: number
    gain: number
    offset: number
}
