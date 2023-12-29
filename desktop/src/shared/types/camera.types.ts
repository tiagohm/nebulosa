import { MessageEvent } from './api.types'
import { Thermometer } from './auxiliary.types'
import { PropertyState } from './device.types'
import { Focuser } from './focuser.types'
import { GuideOutput } from './guider.types'
import { FilterWheel } from './wheel.types'

export type CameraDialogMode = 'CAPTURE' | 'SEQUENCER' | 'FLAT_WIZARD'

export type FrameType = 'LIGHT' | 'DARK' | 'FLAT' | 'BIAS'

export type CfaPattern = 'RGGB' | 'BGGR' | 'GBRG' | 'GRBG' | 'GRGB' | 'GBGR' | 'RGBG' | 'BGRG'

export type AutoSubFolderMode = 'OFF' | 'NOON' | 'MIDNIGHT'

export type ExposureMode = 'SINGLE' | 'FIXED' | 'LOOP'

export enum ExposureTimeUnit {
    MINUTE = 'm',
    SECOND = 's',
    MILLISECOND = 'ms',
    MICROSECOND = 'µs',
}

export interface Camera extends GuideOutput, Thermometer {
    exposuring: boolean
    hasCoolerControl: boolean
    coolerPower: number
    cooler: boolean
    hasDewHeater: boolean
    dewHeater: boolean
    frameFormats: string[]
    canAbort: boolean
    cfaOffsetX: number
    cfaOffsetY: number
    cfaType: CfaPattern
    exposureMin: number
    exposureMax: number
    exposureState: PropertyState
    exposureTime: number
    hasCooler: boolean
    canSetTemperature: boolean
    canSubFrame: boolean
    x: number
    minX: number
    maxX: number
    y: number
    minY: number
    maxY: number
    width: number
    minWidth: number
    maxWidth: number
    height: number
    minHeight: number
    maxHeight: number
    canBin: boolean
    maxBinX: number
    maxBinY: number
    binX: number
    binY: number
    gain: number
    gainMin: number
    gainMax: number
    offset: number
    offsetMin: number
    offsetMax: number
    hasGuiderHead: boolean
    pixelSizeX: number
    pixelSizeY: number
    capturesPath: string
}

export const EMPTY_CAMERA: Camera = {
    exposuring: false,
    hasCoolerControl: false,
    coolerPower: 0,
    cooler: false,
    hasDewHeater: false,
    dewHeater: false,
    frameFormats: [],
    canAbort: false,
    cfaOffsetX: 0,
    cfaOffsetY: 0,
    cfaType: 'RGGB',
    exposureMin: 0,
    exposureMax: 1,
    exposureState: 'IDLE',
    exposureTime: 1,
    hasCooler: false,
    canSetTemperature: false,
    canSubFrame: false,
    x: 0,
    minX: 0,
    maxX: 0,
    y: 0,
    minY: 0,
    maxY: 0,
    width: 1023,
    minWidth: 1023,
    maxWidth: 1023,
    height: 1280,
    minHeight: 1280,
    maxHeight: 1280,
    canBin: false,
    maxBinX: 1,
    maxBinY: 1,
    binX: 1,
    binY: 1,
    gain: 0,
    gainMin: 0,
    gainMax: 0,
    offset: 0,
    offsetMin: 0,
    offsetMax: 0,
    hasGuiderHead: false,
    pixelSizeX: 0,
    pixelSizeY: 0,
    capturesPath: '',
    canPulseGuide: false,
    pulseGuiding: false,
    name: '',
    connected: false,
    hasThermometer: false,
    temperature: 0
}

export interface Dither {
    enabled: boolean
    amount: number
    raOnly: boolean
    afterExposures: number
}

export interface CameraStartCapture {
    enabled?: boolean
    camera?: Camera
    exposureTime: number
    exposureAmount: number
    exposureDelay: number
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
    autoSave: boolean
    savePath?: string
    autoSubFolderMode: AutoSubFolderMode
    dither?: Dither
    wheel?: FilterWheel
    filterPosition?: number
    shutterPosition?: number
    focuser?: Focuser
    focusOffset?: number
}

export const EMPTY_CAMERA_START_CAPTURE: CameraStartCapture = {
    exposureTime: 1,
    exposureAmount: 1,
    exposureDelay: 0,
    x: 0,
    y: 0,
    width: 0,
    height: 0,
    frameType: 'LIGHT',
    binX: 1,
    binY: 1,
    gain: 0,
    offset: 0,
    autoSave: false,
    autoSubFolderMode: 'OFF',
    dither: {
        enabled: false,
        afterExposures: 1,
        amount: 1.5,
        raOnly: false,
    }
}

export interface CameraCaptureEvent extends MessageEvent {
    camera: Camera
    exposureAmount: number
    exposureCount: number
    captureElapsedTime: number
    captureProgress: number
    captureRemainingTime: number
    exposureProgress: number
    exposureRemainingTime: number
    waitRemainingTime: number
    waitProgress: number
    savePath?: string
    state: CameraCaptureState
}

export type CameraCaptureState = 'CAPTURE_STARTED' |
    'EXPOSURE_STARTED' |
    'EXPOSURING' |
    'WAITING' |
    'SETTLING' |
    'EXPOSURE_FINISHED' |
    'CAPTURE_FINISHED'

export interface CameraDialogInput {
    mode: CameraDialogMode
    request: CameraStartCapture
}

export function cameraPreferenceKey(camera: Camera) {
    return `camera.${camera.name}`
}

export interface CameraPreference extends Partial<CameraStartCapture> {
    setpointTemperature?: number
    exposureTimeUnit?: ExposureTimeUnit
    exposureMode?: ExposureMode
    subFrame?: boolean
}

export interface CameraExposureInfo {
    count: number
    remainingTime: number
    progress: number
}

export const EMPTY_CAMERA_EXPOSURE_INFO: CameraExposureInfo = {
    count: 0,
    remainingTime: 0,
    progress: 0,
}

export interface CameraCaptureInfo {
    looping: boolean
    amount: number
    remainingTime: number
    elapsedTime: number
    progress: number
}

export const EMPTY_CAMERA_CAPTURE_INFO: CameraCaptureInfo = {
    looping: false,
    amount: 0,
    remainingTime: 0,
    elapsedTime: 0,
    progress: 0,
}

export interface CameraWaitInfo {
    remainingTime: number
    progress: number
}

export const EMPTY_CAMERA_WAIT_INFO: CameraWaitInfo = {
    remainingTime: 0,
    progress: 0,
}
