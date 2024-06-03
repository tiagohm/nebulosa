import { MessageEvent } from './api.types'
import { Thermometer } from './auxiliary.types'
import { CompanionDevice, Device, PropertyState, isCompanionDevice } from './device.types'
import { GuideOutput } from './guider.types'

export type CameraDialogMode = 'CAPTURE' | 'SEQUENCER' | 'FLAT_WIZARD' | 'TPPA' | 'DARV' | 'AUTO_FOCUS'

export type FrameType = 'LIGHT' | 'DARK' | 'FLAT' | 'BIAS'

export type CfaPattern = 'RGGB' | 'BGGR' | 'GBRG' | 'GRBG' | 'GRGB' | 'GBGR' | 'RGBG' | 'BGRG'

export type AutoSubFolderMode = 'OFF' | 'NOON' | 'MIDNIGHT'

export type ExposureMode = 'SINGLE' | 'FIXED' | 'LOOP'

export type LiveStackerType = 'SIRIL'

export enum ExposureTimeUnit {
    MINUTE = 'm',
    SECOND = 's',
    MILLISECOND = 'ms',
    MICROSECOND = 'µs',
}

export function isCamera(device?: Device): device is Camera {
    return !!device && 'exposuring' in device
}

export function isGuideHead(device?: Device): device is GuideHead {
    return isCamera(device) && isCompanionDevice(device) && !!device.main
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
    hasGuideHead: boolean
    pixelSizeX: number
    pixelSizeY: number
    capturesPath: string
    guideHead?: Device
}

export interface GuideHead extends Camera, CompanionDevice<Camera> {

}

export const EMPTY_CAMERA: Camera = {
    sender: '',
    id: '',
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
    hasGuideHead: false,
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
    dither: Dither
    filterPosition?: number
    shutterPosition?: number
    focusOffset?: number
    calibrationGroup?: string
    liveStacking: LiveStackingRequest
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
    },
    liveStacking: {
        enabled: false,
        type: 'SIRIL',
        executablePath: "",
        rotate: 0,
        use32Bits: false,
    }
}

export function updateCameraStartCaptureFromCamera(request: CameraStartCapture, camera: Camera) {
    if (camera.maxX > 1) request.x = Math.max(camera.minX, Math.min(request.x, camera.maxX))
    if (camera.maxY > 1) request.y = Math.max(camera.minY, Math.min(request.y, camera.maxY))

    if (camera.maxWidth > 1 && (request.width <= 1 || request.width > camera.maxWidth)) request.width = camera.maxWidth
    if (camera.maxHeight > 1 && (request.height <= 1 || request.height > camera.maxHeight)) request.height = camera.maxHeight
    if (camera.minWidth > 1 && request.width < camera.minWidth) request.width = camera.minWidth
    if (camera.minHeight > 1 && request.height < camera.minHeight) request.height = camera.minHeight

    if (camera.maxBinX > 1) request.binX = Math.max(1, Math.min(request.binX, camera.maxBinX))
    if (camera.maxBinY > 1) request.binY = Math.max(1, Math.min(request.binY, camera.maxBinY))
    if (camera.gainMax) request.gain = Math.max(camera.gainMin, Math.min(request.gain, camera.gainMax))
    if (camera.offsetMax) request.offset = Math.max(camera.offsetMin, Math.min(request.offset, camera.offsetMax))
    if (camera.frameFormats.length && (!request.frameFormat || !camera.frameFormats.includes(request.frameFormat))) request.frameFormat = camera.frameFormats[0]
}

export interface CameraCaptureEvent extends MessageEvent {
    camera: Camera
    exposureAmount: number
    exposureCount: number
    captureElapsedTime: number
    captureProgress: number
    captureRemainingTime: number
    stepElapsedTime: number
    stepProgress: number
    stepRemainingTime: number
    savedPath?: string
    liveStackedSavedPath?: string
    state: CameraCaptureState
    capture?: CameraStartCapture
}

export type CameraCaptureState = 'IDLE' | 'CAPTURE_STARTED' | 'EXPOSURE_STARTED' | 'EXPOSURING' | 'WAITING' | 'SETTLING' | 'EXPOSURE_FINISHED' | 'CAPTURE_FINISHED'

export interface CameraDialogInput {
    mode: CameraDialogMode
    camera: Camera
    request: CameraStartCapture
}

export interface CameraPreference extends CameraStartCapture {
    setpointTemperature: number
    exposureTimeUnit: ExposureTimeUnit
    exposureMode: ExposureMode
    subFrame: boolean
}

export const EMPTY_CAMERA_PREFERENCE: CameraPreference = {
    ...EMPTY_CAMERA_START_CAPTURE,
    setpointTemperature: 0,
    exposureTimeUnit: ExposureTimeUnit.MICROSECOND,
    exposureMode: 'SINGLE',
    subFrame: false,
}

export interface CameraStepInfo {
    remainingTime: number
    progress: number
    elapsedTime: number
}

export const EMPTY_CAMERA_STEP_INFO: CameraStepInfo = {
    remainingTime: 0,
    progress: 0,
    elapsedTime: 0,
}

export interface CameraCaptureInfo {
    looping: boolean
    amount: number
    remainingTime: number
    elapsedTime: number
    progress: number
    count: number
}

export const EMPTY_CAMERA_CAPTURE_INFO: CameraCaptureInfo = {
    looping: false,
    amount: 0,
    remainingTime: 0,
    elapsedTime: 0,
    progress: 0,
    count: 0,
}

export interface LiveStackingRequest {
    enabled: boolean,
    type: LiveStackerType,
    executablePath: string,
    dark?: string,
    flat?: string,
    rotate: number,
    use32Bits: boolean,
}

export const EMPTY_LIVE_STACKING_REQUEST: LiveStackingRequest = {
    enabled: false,
    type: 'SIRIL',
    executablePath: '',
    rotate: 0,
    use32Bits: false
}
