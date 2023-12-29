import { AstronomicalObject, DeepSkyObject, EquatorialCoordinateJ2000, Star } from './atlas.types'
import { Camera } from './camera.types'

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
    rightAscension?: string
    declination?: string
    solved: boolean
    headers: FITSHeaderItem[]
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
