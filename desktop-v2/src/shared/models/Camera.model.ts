import { CfaPattern } from '../types/CfaPattern.type'
import { PropertyState } from '../types/PropertyState.type'

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
