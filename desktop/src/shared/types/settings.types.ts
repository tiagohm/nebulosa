export type PlateSolverType = 'ASTROMETRY_NET' | 'ASTROMETRY_NET_ONLINE' | 'ASTAP' | 'SIRIL'

export interface PlateSolverOptions {
    type: PlateSolverType
    executablePath: string
    downsampleFactor: number
    apiUrl: string
    apiKey: string
    timeout: number
}

export const EMPTY_PLATE_SOLVER_OPTIONS: PlateSolverOptions = {
    type: 'ASTAP',
    executablePath: '',
    downsampleFactor: 0,
    apiUrl: 'https://nova.astrometry.net/',
    apiKey: '',
    timeout: 300,
}

export type StarDetectorType = 'ASTAP' | 'PIXINSIGHT'

export interface StarDetectionOptions {
    type: StarDetectorType
    executablePath: string
    timeout: number
    minSNR: number
    slot: number
}

export const EMPTY_STAR_DETECTION_OPTIONS: StarDetectionOptions = {
    type: 'ASTAP',
    executablePath: '',
    timeout: 300,
    minSNR: 0,
    slot: 1,
}
