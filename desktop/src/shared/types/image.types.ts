import { Point, Size } from 'electron'
import { Angle, AstronomicalObject, DeepSkyObject, EquatorialCoordinateJ2000, Star } from './atlas.types'
import { Camera } from './camera.types'
import { PlateSolverType } from './settings.types'

export type ImageChannel = 'RED' | 'GREEN' | 'BLUE' | 'GRAY' | 'NONE'

export const SCNR_PROTECTION_METHODS = ['MAXIMUM_MASK', 'ADDITIVE_MASK', 'AVERAGE_NEUTRAL', 'MAXIMUM_NEUTRAL', 'MINIMUM_NEUTRAL'] as const
export type SCNRProtectionMethod = (typeof SCNR_PROTECTION_METHODS)[number]

export type ImageSource = 'FRAMING' | 'PATH' | 'CAMERA' | 'FLAT_WIZARD'

export interface FITSHeaderItem {
    name: string
    value: string
}

export interface ImageInfo {
    camera: Camera
    path: string
    width: number
    height: number
    mono: boolean
    stretchShadow: number
    stretchHighlight: number
    stretchMidtone: number
    rightAscension?: Angle
    declination?: Angle
    solved?: ImageSolved
    headers: FITSHeaderItem[]
    statistics: ImageStatistics
}

export interface ImageAnnotation {
    x: number
    y: number
    star?: Star
    dso?: DeepSkyObject
    minorPlanet?: AstronomicalObject
}

export interface ImageSolved extends EquatorialCoordinateJ2000 {
    orientation: number
    scale: number
    width: number
    height: number
    radius: number
}

export const EMPTY_IMAGE_SOLVED: ImageSolved = {
    orientation: 0,
    scale: 0,
    width: 0,
    height: 0,
    radius: 0,
    rightAscensionJ2000: '00h00m00s',
    declinationJ2000: '+000°00\'00"'
}

export interface CoordinateInterpolation {
    ma: number[]
    md: number[]
    x0: number
    y0: number
    x1: number
    y1: number
    delta: number
    date?: string
}

export interface DetectedStar {
    x: number
    y: number
    snr: number
    hfd: number
    flux: number
}

export interface ImageStatisticsBitOption {
    name: string,
    rangeMax: number
    bitLength: number
}

export interface ImageStatistics {
    count: number
    maxCount: number
    mean: number
    sumOfSquares: number
    median: number
    variance: number
    stdDev: number
    avgDev: number
    minimum: number
    maximum: number
}

export interface ImagePreference {
    solverRadius?: number
    solverType?: PlateSolverType
}

export const EMPTY_IMAGE_PREFERENCE: ImagePreference = {
    solverRadius: 4,
    solverType: 'ASTROMETRY_NET_ONLINE'
}

export interface ImageData {
    camera?: Camera
    path?: string
    source?: ImageSource
    title?: string
}

export interface FOV {
    enabled: boolean
    focalLength: number
    aperture: number
    cameraSize: Size
    pixelSize: Size
    barlowReducer: number
    bin: number
    rotation: number
    color: string
    computed?: {
        cameraResolution: Size
        focalRatio: number
        fieldSize: Size
        svg: Size & Point
    }
}

export const DEFAULT_FOV: FOV = {
    enabled: true,
    focalLength: 600,
    aperture: 80,
    cameraSize: {
        width: 1392,
        height: 1040,
    },
    pixelSize: {
        width: 6.45,
        height: 6.45,
    },
    barlowReducer: 1,
    bin: 1,
    rotation: 0,
    color: '#FFFF00',
}

export interface FOVEquipment {
    id: number
    name: string
}

export interface FOVCamera extends FOVEquipment {
    sensor: string
    width: number
    height: number
    pixelSize: number
}

export interface FOVTelescope extends FOVEquipment {
    aperture: number
    focalLength: number
}
