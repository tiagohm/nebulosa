import { CfaPattern } from '../enums/cfa-pattern.enum'
import { PropertyState } from '../enums/property-state.enum'
import { FrameFormat } from './frame-format.model'

export interface Camera {
  isConnected: boolean
  name: string
  hasCoolerControl: boolean
  isCoolerOn: boolean
  frameFormats: FrameFormat[]
  canAbort: boolean
  cfaOffsetX: number
  cfaOffsetY: number
  cfaType: CfaPattern
  exposureMin: number
  exposureMax: number
  exposureState: PropertyState
  hasCooler: boolean
  canSetTemperature: boolean
  temperature: number
  canSubframe: boolean
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
  temperatureSetpoint: number
  isCapturing: boolean
  latestCaptureDate: number
  latestCapturePath: string
}
