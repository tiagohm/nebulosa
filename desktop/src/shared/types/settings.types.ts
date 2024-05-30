export type PlateSolverType = 'ASTROMETRY_NET' | 'ASTROMETRY_NET_ONLINE' | 'ASTAP'

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
    timeout: 600,
}

export type StarDetectorType = 'ASTAP'

export interface StarDetectionOptions {
    type: StarDetectorType
    executablePath: string
    timeout: number
}

export const EMPTY_STAR_DETECTION_OPTIONS: StarDetectionOptions = {
    type: 'ASTAP',
    executablePath: '',
    timeout: 600,
}
