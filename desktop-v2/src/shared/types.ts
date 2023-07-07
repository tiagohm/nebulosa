export interface Camera {
    name: string
    connected: boolean
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
    exposure: number
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
    canPulseGuide: boolean
    pulseGuiding: boolean
    hasThermometer: boolean
    temperature: number
}

export interface CameraPreference {
    autoSave: boolean
    savePath: string
    autoSubFolderMode: AutoSubFolderMode
}

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

export interface OpenWindow {
    id: string
    path: string
    icon?: string
    resizable?: boolean
    width?: number | string
    height?: number | string
    bringToFront?: boolean
    requestFocus?: boolean
    params?: Record<string, any>
}

export interface SavedCameraImage {
    id: number
    name: string
    path: string
    width: number
    height: number
    mono: boolean
    savedAt: number
}

export enum ExposureTimeUnit {
    MINUTE = 'm',
    SECOND = 's',
    MILLISECOND = 'ms',
    MICROSECOND = 'µs',
}

export type AutoSubFolderMode = 'OFF' |
    'NOON' |
    'MIDNIGHT'

export type CfaPattern = 'RGGB' |
    'BGGR' |
    'GBRG' |
    'GRBG' |
    'GRGB' |
    'GBGR' |
    'RGBG' |
    'BGRG'

export type ExposureMode = 'SINGLE' |
    'FIXED' |
    'LOOP'

export type FrameType = 'LIGHT' |
    'DARK' |
    'FLAT' |
    'BIAS'

export type PropertyState = 'IDLE' |
    'OK' |
    'BUSY' |
    'ALERT'
