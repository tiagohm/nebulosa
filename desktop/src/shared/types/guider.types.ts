import { Device } from './device.types'

export type GuideDirection = 'NORTH' | // DEC+
    'SOUTH' | // DEC-
    'WEST' | // RA+
    'EAST' // RA-

export const GUIDE_STATES = ['STOPPED', 'SELECTED', 'CALIBRATING', 'GUIDING', 'LOST_LOCK', 'PAUSED', 'LOOPING'] as const
export type GuideState = (typeof GUIDE_STATES)[number]

export const GUIDER_TYPES = ['PHD2'] as const
export type GuiderType = (typeof GUIDER_TYPES)[number]

export type GuiderPlotMode = 'RA/DEC' | 'DX/DY'

export type GuiderYAxisUnit = 'ARCSEC' | 'PIXEL'

export interface GuidePoint {
    x: number
    y: number
}

export interface GuideStep {
    frame: number
    starMass: number
    snr: number
    hfd: number
    dx: number
    dy: number
    raDistance: number
    decDistance: number
    raDistanceGuide: number
    decDistanceGuide: number
    raDuration: number
    raDirection: GuideDirection
    decDuration: number
    decDirection: GuideDirection
    averageDistance: number
}

export interface GuideStar {
    lockPosition: GuidePoint
    starPosition: GuidePoint
    image: string
    guideStep: GuideStep
}

export interface GuiderHistoryStep {
    id: number
    rmsRA: number
    rmsDEC: number
    rmsTotal: number
    guideStep?: GuideStep
    ditherX: number
    ditherY: number
}

export interface GuideOutput extends Device {
    canPulseGuide: boolean
    pulseGuiding: boolean
}

export const EMPTY_GUIDE_OUTPUT: GuideOutput = {
    canPulseGuide: false,
    pulseGuiding: false,
    name: '',
    connected: false
}

export interface Guider {
    connected: boolean
    state: GuideState
    settling: boolean
    pixelScale: number
}

export function reverseGuideDirection(direction: GuideDirection): GuideDirection {
    switch (direction) {
        case 'NORTH': return 'SOUTH'
        case 'SOUTH': return 'NORTH'
        case 'WEST': return 'EAST'
        case 'EAST': return 'WEST'
    }
}

export interface SettleInfo {
    amount: number
    time: number
    timeout: number
}

export interface GuiderMessageEvent<T> extends MessageEvent {
    data: T
}
